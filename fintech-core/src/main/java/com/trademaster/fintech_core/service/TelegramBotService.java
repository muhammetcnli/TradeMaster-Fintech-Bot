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
     * Main entry point for processing Telegram webhook updates.
     * Flow: validate → extract user → parse command → dispatch → respond
     */
    public void handleUpdate(TelegramUpdateDto update) {
        if (update == null || update.getMessage() == null || update.getMessage().getChat() == null) {
            return;
        }

        Long chatId = update.getMessage().getChat().getId();
        String text = update.getMessage().getText();

        if (chatId == null || text == null || text.isBlank()) {
            return;
        }

        logger.debug("Handling Telegram update - Chat ID: {}, Text: {}", chatId, text);

        String[] parts = text.trim().split("\\s+");
        String command = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        // Resolve Telegram user to system user
        Long telegramUserId = extractTelegramUserId(update, chatId);
        String telegramUsername = extractTelegramUsername(update);
        User user = userService.findOrCreateTelegramUser(telegramUserId, telegramUsername);

        logger.debug("Resolved user: {} (id: {}, telegram: {})", user.getUsername(), user.getId(), telegramUserId);

        // Dispatch to handler
        Optional<CommandHandler> handler = commandDispatcher.getHandler(command);
        if (handler.isPresent()) {
            try {
                String response = handler.get().handle(user, update, args);
                if (response != null && !response.isBlank()) {
                    sendMessage(chatId, response);
                }
            } catch (Exception ex) {
                logger.error("Error executing command {} for user {}: {}", command, user.getUsername(), ex.getMessage(), ex);
                sendMessage(chatId, "❌ Error: " + ex.getMessage());
            }
        } else {
            sendMessage(chatId, "Unknown command: " + command + "\nType /help for available commands.");
        }
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
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), String.class);
        } catch (Exception ex) {
            logger.error("Failed to send Telegram message to chat {}: {}", chatId, ex.getMessage());
        }
    }
}
