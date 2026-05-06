package com.trademaster.fintech_core.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Validates internal bot-to-API security headers.
 *
 * Endpoints marked for internal-only access require:
 * - X-Bot-Secret header matching configured secret
 * - X-Telegram-UserId header (for user context in logs/audit)
 *
 * This is NOT for end-user auth; it's for bot middleware authentication.
 */
@Component
public class InternalBotSecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(InternalBotSecurityFilter.class);
    private static final String BOT_SECRET_HEADER = "X-Bot-Secret";
    private static final String TELEGRAM_USER_ID_HEADER = "X-Telegram-UserId";

    @Value("${telegram.bot.internal-secret:}")
    private String configuredBotSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only validate "internal" endpoints (e.g., /api/v1/internal/*)
        if (path.startsWith("/api/v1/internal/")) {
            String providedSecret = request.getHeader(BOT_SECRET_HEADER);
            String telegramUserId = request.getHeader(TELEGRAM_USER_ID_HEADER);

            // If internal secret is configured, enforce it
            if (!configuredBotSecret.isBlank()) {
                if (providedSecret == null || !providedSecret.equals(configuredBotSecret)) {
                    logger.warn("Rejected internal request: invalid or missing X-Bot-Secret from {}", request.getRemoteAddr());
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("{\"error\": \"Invalid or missing bot secret\"}");
                    return;
                }
            }

            // Log telegram context if provided
            if (telegramUserId != null && !telegramUserId.isBlank()) {
                logger.debug("Internal bot call from Telegram user: {}", telegramUserId);
            }
        }

        filterChain.doFilter(request, response);
    }
}

