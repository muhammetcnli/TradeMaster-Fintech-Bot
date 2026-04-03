package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.MarketPriceDto;
import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramBotService {

    private final AuthService authService;
    private final MarketDataService marketDataService;
    private final PortfolioService portfolioService;
    private final RestTemplate restTemplate;
    private final Map<UUID, Long> userChatIndex = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, PriceAlert>> alertsByUser = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, AutoTradeRule>> autoRulesByUser = new ConcurrentHashMap<>();

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramBotService(AuthService authService,
                              MarketDataService marketDataService,
                              PortfolioService portfolioService,
                              RestTemplate restTemplate) {
        this.authService = authService;
        this.marketDataService = marketDataService;
        this.portfolioService = portfolioService;
        this.restTemplate = restTemplate;
    }

    public void handleUpdate(TelegramUpdateDto update) {
        if (update == null || update.getMessage() == null || update.getMessage().getChat() == null) {
            return;
        }

        Long chatId = update.getMessage().getChat().getId();
        String text = update.getMessage().getText();

        if (chatId == null || text == null || text.isBlank()) {
            return;
        }

        String[] parts = text.trim().split("\\s+");
        String command = parts[0].toLowerCase();

        try {
            switch (command) {
                case "/start" -> handleStart(chatId, update);
                case "/price" -> handlePrice(chatId, parts);
                case "/watch" -> handleWatch(chatId, parts, update);
                case "/watchlist" -> handleWatchlist(chatId, update);
                case "/portfolio" -> handlePortfolio(chatId, update);
                case "/buy" -> handleBuy(chatId, parts, update);
                case "/sell" -> handleSell(chatId, parts, update);
                case "/alert" -> handleAlert(chatId, parts, update);
                case "/autobuy" -> handleAutoTrade(chatId, parts, update, TradeAction.BUY);
                case "/autosell" -> handleAutoTrade(chatId, parts, update, TradeAction.SELL);
                case "/rules" -> handleRules(chatId, update);
                default -> sendMessage(chatId, "Unknown command. Try: /start, /price BTC, /watch BTC, /watchlist, /portfolio, /buy BTC 0.1, /sell BTC 0.1, /alert BTC 80000 [UP|DOWN], /autobuy BTC 70000 0.01, /autosell BTC 90000 0.01, /rules");
            }
        } catch (Exception ex) {
            sendMessage(chatId, "Error: " + ex.getMessage());
        }
    }

    
    private void handleStart(Long chatId, TelegramUpdateDto update) {
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);
        sendMessage(chatId, "Welcome back. Your account is linked securely with this Telegram chat.");
    }

    private void handlePrice(Long chatId, String[] parts) {
        if (parts.length < 2) {
            sendMessage(chatId, "Usage: /price <symbol>");
            return;
        }
        String symbol = parts[1].trim().toUpperCase();
        MarketPriceDto price = marketDataService.getCurrentPrice(symbol);
        sendMessage(chatId, symbol + " = " + price.getPrice() + " " + price.getCurrency());
    }

    private void handleWatch(Long chatId, String[] parts, TelegramUpdateDto update) throws IllegalAccessException {
        if (parts.length < 2) {
            sendMessage(chatId, "Usage: /watch <symbol>");
            return;
        }
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);
        String symbol = parts[1].trim().toUpperCase();
        portfolioService.watchAsset(userId, symbol);
        sendMessage(chatId, symbol + " added to watchlist.");
    }

    private void handleWatchlist(Long chatId, TelegramUpdateDto update) {
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);

        List<com.trademaster.fintech_core.dto.WatchListDto> watchList = portfolioService.getWatchList(userId);
        if (watchList.isEmpty()) {
            sendMessage(chatId, "Watchlist is empty. Use /watch <symbol>.");
            return;
        }

        StringBuilder sb = new StringBuilder("Watchlist:\n");
        for (com.trademaster.fintech_core.dto.WatchListDto item : watchList) {
            sb.append("- ")
                    .append(item.getSymbol())
                    .append(": ")
                    .append(item.getCurrentPrice())
                    .append(" USD\n");
        }
        sendMessage(chatId, sb.toString().trim());
    }

    private void handlePortfolio(Long chatId, TelegramUpdateDto update) {
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);
        PortfolioDto portfolio = portfolioService.getUserPortfolio(userId);
        sendMessage(chatId,
                "Balance: " + portfolio.getCurrentBalance() + "\n" +
                "Total Value: " + portfolio.getTotalPortfolioValue() + "\n" +
                "Asset Count: " + portfolio.getAssets().size());
    }

    private void handleBuy(Long chatId, String[] parts, TelegramUpdateDto update) throws IllegalAccessException {
        if (parts.length < 3) {
            sendMessage(chatId, "Usage: /buy <symbol> <quantity>");
            return;
        }
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);
        String symbol = parts[1].trim().toUpperCase();
        BigDecimal quantity = new BigDecimal(parts[2]);
        portfolioService.buyAsset(userId, symbol, quantity);
        sendMessage(chatId, "Bought " + quantity + " " + symbol);
    }

    private void handleSell(Long chatId, String[] parts, TelegramUpdateDto update) throws IllegalAccessException {
        if (parts.length < 3) {
            sendMessage(chatId, "Usage: /sell <symbol> <quantity>");
            return;
        }
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);
        String symbol = parts[1].trim().toUpperCase();
        BigDecimal quantity = new BigDecimal(parts[2]);
        portfolioService.sellAsset(userId, symbol, quantity);
        sendMessage(chatId, "Sold " + quantity + " " + symbol);
    }

    private void handleAlert(Long chatId, String[] parts, TelegramUpdateDto update) {
        if (parts.length < 3) {
            sendMessage(chatId, "Usage: /alert <symbol> <targetPrice> [UP|DOWN]");
            return;
        }

        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);

        String symbol = parts[1].trim().toUpperCase();
        BigDecimal targetPrice = new BigDecimal(parts[2]);
        TriggerDirection direction = TriggerDirection.UP;
        if (parts.length >= 4) {
            direction = TriggerDirection.valueOf(parts[3].trim().toUpperCase());
        }

        alertsByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(symbol, new PriceAlert(symbol, targetPrice, direction));

        sendMessage(chatId, "Alert set: " + symbol + " " + direction + " " + targetPrice + " USD");
    }

    private void handleAutoTrade(Long chatId, String[] parts, TelegramUpdateDto update, TradeAction action) {
        if (parts.length < 4) {
            sendMessage(chatId, "Usage: /" + action.name().toLowerCase() + " <symbol> <targetPrice> <quantity>");
            return;
        }

        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);

        String symbol = parts[1].trim().toUpperCase();
        BigDecimal targetPrice = new BigDecimal(parts[2]);
        BigDecimal quantity = new BigDecimal(parts[3]);

        autoRulesByUser
                .computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(symbol + "-" + action.name(), new AutoTradeRule(symbol, targetPrice, quantity, action));

        String condition = action == TradeAction.BUY ? "<=" : ">=";
        sendMessage(chatId, "Auto " + action.name().toLowerCase() + " set: " + symbol + " when price " + condition + " " + targetPrice + " USD, qty=" + quantity);
    }

    private void handleRules(Long chatId, TelegramUpdateDto update) {
        UUID userId = resolveUserId(chatId, update);
        userChatIndex.put(userId, chatId);

        Map<String, PriceAlert> alerts = alertsByUser.getOrDefault(userId, Map.of());
        Map<String, AutoTradeRule> rules = autoRulesByUser.getOrDefault(userId, Map.of());

        if (alerts.isEmpty() && rules.isEmpty()) {
            sendMessage(chatId, "No alerts or auto-trade rules.");
            return;
        }

        StringBuilder sb = new StringBuilder("Rules:\n");
        alerts.values().forEach(alert -> sb.append("ALERT ")
                .append(alert.symbol)
                .append(" ")
                .append(alert.direction)
                .append(" ")
                .append(alert.targetPrice)
                .append("\n"));
        rules.values().forEach(rule -> sb.append("AUTO ")
                .append(rule.action)
                .append(" ")
                .append(rule.symbol)
                .append(" target=")
                .append(rule.targetPrice)
                .append(" qty=")
                .append(rule.quantity)
                .append(" active=")
                .append(rule.active)
                .append("\n"));

        sendMessage(chatId, sb.toString().trim());
    }

    @Scheduled(fixedDelayString = "${telegram.automation.poll-ms:30000}")
    public void evaluateAlertsAndAutoTrades() {
        for (Map.Entry<UUID, Long> entry : userChatIndex.entrySet()) {
            UUID userId = entry.getKey();
            Long chatId = entry.getValue();

            Map<String, PriceAlert> alerts = alertsByUser.getOrDefault(userId, Map.of());
            for (PriceAlert alert : alerts.values()) {
                if (alert.triggered) {
                    continue;
                }

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
                    sendMessage(chatId, "Alert triggered: " + alert.symbol + " price=" + currentPrice + " USD");
                }
            }

            Map<String, AutoTradeRule> rules = autoRulesByUser.getOrDefault(userId, Map.of());
            for (AutoTradeRule rule : rules.values()) {
                if (!rule.active) {
                    continue;
                }

                BigDecimal currentPrice;
                try {
                    currentPrice = marketDataService.getCurrentPrice(rule.symbol).getPrice();
                } catch (Exception ex) {
                    continue;
                }

                boolean shouldExecute = rule.action == TradeAction.BUY
                        ? currentPrice.compareTo(rule.targetPrice) <= 0
                        : currentPrice.compareTo(rule.targetPrice) >= 0;

                if (!shouldExecute) {
                    continue;
                }

                try {
                    if (rule.action == TradeAction.BUY) {
                        portfolioService.buyAsset(userId, rule.symbol, rule.quantity);
                    } else {
                        portfolioService.sellAsset(userId, rule.symbol, rule.quantity);
                    }
                    rule.active = false;
                    sendMessage(chatId, "Auto " + rule.action.name().toLowerCase() + " executed: " + rule.quantity + " " + rule.symbol + " at " + currentPrice + " USD");
                } catch (Exception ex) {
                    sendMessage(chatId, "Auto " + rule.action.name().toLowerCase() + " failed for " + rule.symbol + ": " + ex.getMessage());
                }
            }
        }
    }

    private UUID resolveUserId(Long chatId, TelegramUpdateDto update) {
        String username = null;
        if (update != null && update.getMessage() != null && update.getMessage().getFrom() != null) {
            username = update.getMessage().getFrom().getUsername();
        }

        String fallbackUsername = "telegram_" + chatId;
        String safeUsername = (username == null || username.isBlank()) ? fallbackUsername : username;

        return authService.registerOrLogin("TELEGRAM", String.valueOf(chatId), safeUsername);
    }

    private enum TriggerDirection {
        UP,
        DOWN
    }

    private enum TradeAction {
        BUY,
        SELL
    }

    private static class PriceAlert {
        private final String symbol;
        private final BigDecimal targetPrice;
        private final TriggerDirection direction;
        private volatile boolean triggered;

        private PriceAlert(String symbol, BigDecimal targetPrice, TriggerDirection direction) {
            this.symbol = symbol;
            this.targetPrice = targetPrice;
            this.direction = direction;
            this.triggered = false;
        }
    }

    private static class AutoTradeRule {
        private final String symbol;
        private final BigDecimal targetPrice;
        private final BigDecimal quantity;
        private final TradeAction action;
        private volatile boolean active;

        private AutoTradeRule(String symbol, BigDecimal targetPrice, BigDecimal quantity, TradeAction action) {
            this.symbol = symbol;
            this.targetPrice = targetPrice;
            this.quantity = quantity;
            this.action = action;
            this.active = true;
        }
    }

    private void sendMessage(Long chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
    }
}

