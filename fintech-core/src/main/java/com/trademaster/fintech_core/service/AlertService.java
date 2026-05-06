package com.trademaster.fintech_core.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<UUID, Map<String, PriceAlert>> alertsByUser = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, AutoTradeRule>> autoRulesByUser = new ConcurrentHashMap<>();

    // ---- Alert Management ----

    public PriceAlert addAlert(UUID userId, String symbol, BigDecimal targetPrice, TriggerDirection direction) {
        PriceAlert alert = new PriceAlert(symbol, targetPrice, direction);
        alertsByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(symbol, alert);
        logger.debug("Alert added for user {}: {} {} {}", userId, symbol, direction, targetPrice);
        return alert;
    }

    public Map<String, PriceAlert> getUserAlerts(UUID userId) {
        return alertsByUser.getOrDefault(userId, Map.of());
    }

    // ---- Auto-Trade Rule Management ----

    public AutoTradeRule addAutoTradeRule(UUID userId, String symbol, BigDecimal targetPrice,
                                          BigDecimal quantity, TradeAction action) {
        AutoTradeRule rule = new AutoTradeRule(symbol, targetPrice, quantity, action);
        autoRulesByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(symbol + "-" + action.name(), rule);
        logger.debug("Auto-trade rule added for user {}: {} {} {} at {}", userId, action, symbol, quantity, targetPrice);
        return rule;
    }

    public Map<String, AutoTradeRule> getUserAutoTradeRules(UUID userId) {
        return autoRulesByUser.getOrDefault(userId, Map.of());
    }

    // ---- Scheduled Evaluation ----

    /**
     * Periodically evaluate all alerts and auto-trade rules.
     * Returns a list of notification messages (userId → message) for triggered events.
     * The caller (TelegramBotService) is responsible for sending messages to users.
     */
    @Scheduled(fixedDelayString = "${telegram.automation.poll-ms:30000}")
    public void evaluateAll() {
        // Evaluate alerts
        for (Map.Entry<UUID, Map<String, PriceAlert>> entry : alertsByUser.entrySet()) {
            UUID userId = entry.getKey();
            for (PriceAlert alert : entry.getValue().values()) {
                if (alert.triggered) continue;

                BigDecimal currentPrice;
                try {
                    currentPrice = marketDataService.getCurrentPrice(alert.symbol).getPrice();
                } catch (Exception ex) {
                    continue;
                }

                boolean hit = alert.direction == TriggerDirection.UP
                        ? currentPrice.compareTo(alert.targetPrice) >= 0
                        : currentPrice.compareTo(alert.targetPrice) <= 0;

                if (hit) {
                    alert.triggered = true;
                    logger.info("Alert triggered for user {}: {} price={}", userId, alert.symbol, currentPrice);
                }
            }
        }

        // Evaluate auto-trade rules
        for (Map.Entry<UUID, Map<String, AutoTradeRule>> entry : autoRulesByUser.entrySet()) {
            UUID userId = entry.getKey();
            for (AutoTradeRule rule : entry.getValue().values()) {
                if (!rule.active) continue;

                BigDecimal currentPrice;
                try {
                    currentPrice = marketDataService.getCurrentPrice(rule.symbol).getPrice();
                } catch (Exception ex) {
                    continue;
                }

                boolean shouldExecute = rule.action == TradeAction.BUY
                        ? currentPrice.compareTo(rule.targetPrice) <= 0
                        : currentPrice.compareTo(rule.targetPrice) >= 0;

                if (!shouldExecute) continue;

                try {
                    if (rule.action == TradeAction.BUY) {
                        portfolioService.buyAsset(userId, rule.symbol, rule.quantity);
                    } else {
                        portfolioService.sellAsset(userId, rule.symbol, rule.quantity);
                    }
                    rule.active = false;
                    logger.info("Auto {} executed for user {}: {} {} at {}",
                            rule.action, userId, rule.quantity, rule.symbol, currentPrice);
                } catch (Exception ex) {
                    logger.error("Auto {} failed for user {}: {} - {}",
                            rule.action, userId, rule.symbol, ex.getMessage());
                }
            }
        }
    }

    // ---- Domain Types ----

    public enum TriggerDirection {
        UP, DOWN
    }

    public enum TradeAction {
        BUY, SELL
    }

    @Getter
    public static class PriceAlert {
        private final String symbol;
        private final BigDecimal targetPrice;
        private final TriggerDirection direction;
        private volatile boolean triggered;

        public PriceAlert(String symbol, BigDecimal targetPrice, TriggerDirection direction) {
            this.symbol = symbol;
            this.targetPrice = targetPrice;
            this.direction = direction;
            this.triggered = false;
        }
    }

    @Getter
    public static class AutoTradeRule {
        private final String symbol;
        private final BigDecimal targetPrice;
        private final BigDecimal quantity;
        private final TradeAction action;
        private volatile boolean active;

        public AutoTradeRule(String symbol, BigDecimal targetPrice, BigDecimal quantity, TradeAction action) {
            this.symbol = symbol;
            this.targetPrice = targetPrice;
            this.quantity = quantity;
            this.action = action;
            this.active = true;
        }
    }
}
