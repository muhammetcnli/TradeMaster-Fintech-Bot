package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.entity.Alert;
import com.trademaster.fintech_core.entity.AlertType;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * Manages price alerts and auto-trade rules for users.
 * Currently uses in-memory storage; will be persisted to DB in a future iteration.
 */
@Service
@RequiredArgsConstructor
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final MarketDataService marketDataService;
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final AlertRepository alertRepository;
    private final com.trademaster.fintech_core.repository.AutoTradeRuleRepository autoTradeRuleRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    // ---- Alert Management ----

    @Transactional
    public Alert addPriceAlert(UUID userId, String symbol, BigDecimal targetPrice, AlertType type) {
        User user = userService.getUserById(userId);
        Alert alert = Alert.builder()
                .user(user)
                .symbol(symbol.toUpperCase())
                .type(type)
                .targetValue(targetPrice)
                .active(true)
                .build();
        
        logger.info("New price alert added for user {}: {} {}", user.getUsername(), symbol, type);
        return alertRepository.save(alert);
    }

    @Transactional
    public Alert addPercentageAlert(UUID userId, String symbol, BigDecimal percentageChange) {
        User user = userService.getUserById(userId);
        BigDecimal currentPrice = marketDataService.getCurrentPrice(symbol).getPrice();
        
        AlertType type = percentageChange.compareTo(BigDecimal.ZERO) >= 0 
                ? AlertType.PERCENT_CHANGE_UP 
                : AlertType.PERCENT_CHANGE_DOWN;

        Alert alert = Alert.builder()
                .user(user)
                .symbol(symbol.toUpperCase())
                .type(type)
                .targetValue(percentageChange.abs())
                .basePrice(currentPrice)
                .active(true)
                .build();

        logger.info("New percentage alert added for user {}: {} {}%", user.getUsername(), symbol, percentageChange);
        return alertRepository.save(alert);
    }

    public List<Alert> getActiveAlerts(UUID userId) {
        return alertRepository.findAllByUserIdAndActiveTrue(userId);
    }

    public List<com.trademaster.fintech_core.entity.AutoTradeRule> getActiveAutoTradeRules(UUID userId) {
        return autoTradeRuleRepository.findAllByActiveTrue().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .toList();
    }

    @Transactional
    public void deleteAlert(UUID userId, UUID alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        
        if (!alert.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        alert.setActive(false);
        alertRepository.save(alert);
    }

    // ---- Auto-Trade Rule Management ----

    @Transactional
    public com.trademaster.fintech_core.entity.AutoTradeRule addAutoTradeRule(UUID userId, String symbol, BigDecimal targetPrice, 
                                                                            BigDecimal quantity, com.trademaster.fintech_core.entity.AutoTradeRule.TradeAction action) {
        User user = userService.getUserById(userId);
        var rule = com.trademaster.fintech_core.entity.AutoTradeRule.builder()
                .user(user)
                .symbol(symbol.toUpperCase())
                .targetPrice(targetPrice)
                .quantity(quantity)
                .action(action)
                .active(true)
                .build();
        
        logger.info("New auto-trade rule added for user {}: {} {} at {}", user.getUsername(), action, symbol, targetPrice);
        return autoTradeRuleRepository.save(rule);
    }

    // ---- Scheduled Evaluation ----

    @Scheduled(fixedDelayString = "${telegram.automation.poll-ms:30000}")
    @Transactional
    public void evaluateAll() {
        evaluateAlerts();
        evaluateAutoTrades();
    }

    private void evaluateAlerts() {
        List<Alert> activeAlerts = alertRepository.findAllByActiveTrue();
        if (activeAlerts.isEmpty()) return;

        for (Alert alert : activeAlerts) {
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(alert.getSymbol()).getPrice();
                if (isTriggered(alert, currentPrice)) {
                    triggerAlert(alert, currentPrice);
                }
            } catch (Exception ex) {
                logger.warn("Failed to evaluate alert {}: {}", alert.getId(), ex.getMessage());
            }
        }
    }

    private void evaluateAutoTrades() {
        List<com.trademaster.fintech_core.entity.AutoTradeRule> rules = autoTradeRuleRepository.findAllByActiveTrue();
        for (var rule : rules) {
            try {
                BigDecimal currentPrice = marketDataService.getCurrentPrice(rule.getSymbol()).getPrice();
                
                boolean shouldExecute = rule.getAction() == com.trademaster.fintech_core.entity.AutoTradeRule.TradeAction.BUY
                        ? currentPrice.compareTo(rule.getTargetPrice()) <= 0
                        : currentPrice.compareTo(rule.getTargetPrice()) >= 0;

                if (shouldExecute) {
                    executeAutoTrade(rule, currentPrice);
                }
            } catch (Exception ex) {
                logger.warn("Failed to evaluate auto-trade rule {}: {}", rule.getId(), ex.getMessage());
            }
        }
    }

    private void executeAutoTrade(com.trademaster.fintech_core.entity.AutoTradeRule rule, BigDecimal currentPrice) {
        rule.setActive(false);
        autoTradeRuleRepository.save(rule);

        try {
            if (rule.getAction() == com.trademaster.fintech_core.entity.AutoTradeRule.TradeAction.BUY) {
                portfolioService.buyAsset(rule.getUser().getId(), rule.getSymbol(), rule.getQuantity());
            } else {
                portfolioService.sellAsset(rule.getUser().getId(), rule.getSymbol(), rule.getQuantity());
            }

            Long chatId = Long.valueOf(rule.getUser().getExternalId());
            String msg = "🤖 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Auto-Trade Executed!") + "\n\n" +
                         "⚡ Action: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(rule.getAction().toString()) + "\n" +
                         "💎 Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(rule.getSymbol()) + "\n" +
                         "📦 Quantity: " + rule.getQuantity() + "\n" +
                         "💵 Price: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold(currentPrice.toString());
            
            eventPublisher.publishEvent(new com.trademaster.fintech_core.telegram.event.TelegramNotificationEvent(this, chatId, msg));
        } catch (Exception ex) {
            logger.error("Auto trade execution failed for user {}: {}", rule.getUser().getId(), ex.getMessage());
        }
    }

    private boolean isTriggered(Alert alert, BigDecimal currentPrice) {
        switch (alert.getType()) {
            case PRICE_ABOVE:
                return currentPrice.compareTo(alert.getTargetValue()) >= 0;
            case PRICE_BELOW:
                return currentPrice.compareTo(alert.getTargetValue()) <= 0;
            case PERCENT_CHANGE_UP:
                return calculatePercentChange(alert.getBasePrice(), currentPrice).compareTo(alert.getTargetValue()) >= 0;
            case PERCENT_CHANGE_DOWN:
                return calculatePercentChange(alert.getBasePrice(), currentPrice).negate().compareTo(alert.getTargetValue()) >= 0;
            default:
                return false;
        }
    }

    private BigDecimal calculatePercentChange(BigDecimal base, BigDecimal current) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return current.subtract(base)
                .divide(base, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void triggerAlert(Alert alert, BigDecimal currentPrice) {
        alert.setActive(false);
        alert.setLastCheckedAt(java.time.Instant.now());
        alertRepository.save(alert);

        try {
            Long chatId = Long.valueOf(alert.getUser().getExternalId());
            String msg = formatAlertMessage(alert, currentPrice);
            eventPublisher.publishEvent(new com.trademaster.fintech_core.telegram.event.TelegramNotificationEvent(this, chatId, msg));
            logger.info("Alert triggered and notified for user {}: {} {}", alert.getUser().getUsername(), alert.getSymbol(), alert.getType());
        } catch (Exception ex) {
            logger.error("Failed to notify user {} about alert trigger", alert.getUser().getId(), ex);
        }
    }

    private String formatAlertMessage(Alert alert, BigDecimal currentPrice) {
        String title = "🔔 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Price Alert Triggered!");
        String asset = "💎 Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(alert.getSymbol());
        String condition = "🎯 Condition: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.italic(formatCondition(alert));
        String current = "💵 Current Price: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold(currentPrice.toString());

        return title + "\n\n" + asset + "\n" + condition + "\n" + current;
    }

    private String formatCondition(Alert alert) {
        switch (alert.getType()) {
            case PRICE_ABOVE: return "Price Above " + alert.getTargetValue();
            case PRICE_BELOW: return "Price Below " + alert.getTargetValue();
            case PERCENT_CHANGE_UP: return "Price Up " + alert.getTargetValue() + "%";
            case PERCENT_CHANGE_DOWN: return "Price Down " + alert.getTargetValue() + "%";
            default: return "Unknown";
        }
    }
}
