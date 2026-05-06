package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.AuthResponse;
import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AuthService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for /start command.
 * - Links Telegram user to system user
 * - Generates and returns bearer token
 * - Provides setup instructions
 */
@Component
@RequiredArgsConstructor
public class StartCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartCommandHandler.class);
    private final AuthService authService;

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        logger.info("Handling /start for user: {} (telegram_id: {})", user.getUsername(), user.getExternalId());

        try {
            // User already exists (passed from middleware), just generate token
            String token = generateNewToken(user);

            String response = String.format(
                    "Welcome back! Your account is linked securely with this Telegram chat.\n\n" +
                    "REST access token: `%s`\n\n" +
                    "Keep this token private. Use it as:\n" +
                    "```\n" +
                    "Authorization: Bearer %s\n" +
                    "```\n\n" +
                    "Available commands:\n" +
                    "/price BTC - Get current price\n" +
                    "/portfolio - View your portfolio\n" +
                    "/watch BTC - Add to watchlist\n" +
                    "/help - Show all commands",
                    token, token
            );

            logger.info("/start completed for userId: {}", user.getId());
            return response;

        } catch (Exception ex) {
            logger.error("Error in /start handler", ex);
            return "❌ Error: " + ex.getMessage();
        }
    }

    /**
     * Generate a new access token for the user
     */
    private String generateNewToken(User user) {
        // This would typically call authService to create a new token
        // For now, we use the existing AuthService flow
        AuthResponse auth = authService.registerOrLogin(
                user.getAuthProvider(),
                user.getExternalId(),
                user.getUsername()
        );
        return auth.accessToken();
    }
}

