package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.PortfolioService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for /watch command.
 * Adds an asset to the user's watchlist.
 */
@Component
@RequiredArgsConstructor
public class WatchCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(WatchCommandHandler.class);
    private final PortfolioService portfolioService;

    @Override
    public String getCommand() {
        return "/watch";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 1) {
            return "Usage: /watch <symbol>\nExample: /watch BTC";
        }

        String symbol = args[0].trim().toUpperCase();
        logger.debug("Watch {} for user {}", symbol, user.getUsername());

        try {
            portfolioService.watchAsset(user.getId(), symbol);
            return String.format("👁 %s added to watchlist.", symbol);
        } catch (Exception ex) {
            logger.error("Watch failed for {}: {}", symbol, ex.getMessage());
            return "❌ Watch failed: " + ex.getMessage();
        }
    }
}
