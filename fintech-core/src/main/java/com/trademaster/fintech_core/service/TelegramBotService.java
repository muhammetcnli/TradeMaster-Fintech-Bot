package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.telegram.CommandDispatcher;
import com.trademaster.fintech_core.telegram.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Core Telegram bot service.
 * Receives webhook updates, resolves the Telegram user to a system user,
 * dispatches commands to the appropriate handler, and sends responses.
 *
 * This service does NOT contain business logic — it is a thin routing layer.
 * All business logic lives in dedicated services (PortfolioService, AlertService, etc.)
 * and is invoked through CommandHandler implementations.
 */
@Service
public class TelegramBotService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotService.class);

    private final UserService userService;
    private final CommandDispatcher commandDispatcher;
    private final RestTemplate restTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramBotService(UserService userService,
                              CommandDispatcher commandDispatcher,
                              RestTemplate restTemplate) {
        this.userService = userService;
        this.commandDispatcher = commandDispatcher;
        this.restTemplate = restTemplate;
    }

    /**
     * Listen for internal notification events and forward them to Telegram.
     */
    @org.springframework.context.event.EventListener
    public void handleNotificationEvent(com.trademaster.fintech_core.telegram.event.TelegramNotificationEvent event) {
        logger.info("Sending asynchronous notification to chatId: {}", event.getTelegramChatId());
        sendMessage(event.getTelegramChatId(), event.getMessage());
    }

    /**
     * Main entry point for processing Telegram webhook updates.
     * Flow: validate → extract user → parse command/callback → dispatch → respond
     */
    public void handleUpdate(TelegramUpdateDto update) {
        if (update == null) return;

        Long chatId;
        String text;
        Long telegramUserId;
        String telegramUsername;

        if (update.getMessage() != null && update.getMessage().getChat() != null) {
            chatId = update.getMessage().getChat().getId();
            text = update.getMessage().getText();
            telegramUserId = extractTelegramUserId(update, chatId);
            telegramUsername = extractTelegramUsername(update);
        } else if (update.getCallbackQuery() != null) {
            chatId = update.getCallbackQuery().getMessage().getChat().getId();
            text = update.getCallbackQuery().getData();
            telegramUserId = update.getCallbackQuery().getFrom().getId();
            telegramUsername = update.getCallbackQuery().getFrom().getUsername();
        } else {
            return;
        }

        if (chatId == null || text == null || text.isBlank()) {
            return;
        }

        // Put user context into MDC for structured logging
        org.slf4j.MDC.put("chatId", String.valueOf(chatId));
        org.slf4j.MDC.put("telegramUserId", String.valueOf(telegramUserId));
        org.slf4j.MDC.put("username", telegramUsername != null ? telegramUsername : "anonymous");

        long startTime = System.currentTimeMillis();
        try {
            logger.info("Processing update: {}", text);

            // Normalize button text to commands
            String commandText = normalizeCommand(text);
            
            // Basic validation
            com.trademaster.fintech_core.telegram.util.TelegramValidator.validatePrompt(commandText);

            String[] parts = commandText.trim().split("\\s+");
            String command = parts[0].toLowerCase();
            String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

            org.slf4j.MDC.put("command", command);

            // Resolve Telegram user to system user
            User user = userService.findOrCreateTelegramUser(telegramUserId, telegramUsername);

            // Dispatch to handler
            Optional<CommandHandler> handler = commandDispatcher.getHandler(command);
            if (handler.isPresent()) {
                String response = handler.get().handle(user, update, args);
                if (response != null && !response.isBlank()) {
                    Object replyMarkup = command.equals("/start") ? com.trademaster.fintech_core.telegram.util.KeyboardFactory.mainMenu() : null;
                    sendMessage(chatId, response, replyMarkup);
                }
            } else {
                logger.warn("Unknown command received: {}", command);
                sendMessage(chatId, "❓ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Unknown command: ") + command + "\nType /help for available commands.");
            }
        } catch (Exception ex) {
            logger.error("Global Error: {}", ex.getMessage(), ex);
            sendMessage(chatId, "⚠️ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Something went wrong.") + "\n" + ex.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Processed in {}ms", duration);
            org.slf4j.MDC.clear();
        }
    }

    private String normalizeCommand(String text) {
        if (text.contains("💰 Price")) return "/price";
        if (text.contains("📊 Portfolio")) return "/portfolio";
        if (text.contains("🔔 Alerts")) return "/alerts";
        if (text.contains("📋 Watchlist")) return "/watchlist";
        if (text.contains("❓ Help")) return "/help";
        return text;
    }

    /**
     * Extract telegram user ID from the update. Falls back to chatId if from.id is not present.
     */
    private Long extractTelegramUserId(TelegramUpdateDto update, Long chatId) {
        if (update.getMessage().getFrom() != null && update.getMessage().getFrom().getId() != null) {
            return update.getMessage().getFrom().getId();
        }
        return chatId;
    }

    /**
     * Extract telegram username from the update.
     */
    private String extractTelegramUsername(TelegramUpdateDto update) {
        if (update.getMessage().getFrom() != null) {
            return update.getMessage().getFrom().getUsername();
        }
        return null;
    }

    /**
     * Send a text message to a Telegram chat via the Bot API.
     */
    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    /**
     * Send a text message with optional keyboard/markup.
     */
    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("parse_mode", "HTML");

        if (replyMarkup != null) {
            payload.put("reply_markup", replyMarkup);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception ex) {
            logger.error("Failed to send Telegram message to chat {}: {}", chatId, ex.getMessage());
            // Fallback: try sending without Markdown if it fails (often due to bad escaping)
            if (payload.get("parse_mode") != null) {
                payload.remove("parse_mode");
                try {
                    restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
                } catch (Exception retryEx) {
                    logger.error("Retry failed for chat {}: {}", chatId, retryEx.getMessage());
                }
            }
        }
    }
}
