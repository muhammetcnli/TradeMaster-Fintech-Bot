package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.PortfolioService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler for /sell command.
 * Sells a specified quantity of an asset.
 */
@Component
@RequiredArgsConstructor
public class SellCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(SellCommandHandler.class);
    private final PortfolioService portfolioService;

    @Override
    public String getCommand() {
        return "/sell";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 2) {
            return "Usage: /sell <symbol> <quantity>\nExample: /sell BTC 0.1";
        }

        String symbol = args[0].trim().toUpperCase();
        BigDecimal quantity;
        try {
            quantity = new BigDecimal(args[1]);
        } catch (NumberFormatException ex) {
            return "❌ Invalid quantity: " + args[1];
        }

        logger.debug("Sell {} {} for user {}", quantity, symbol, user.getUsername());

        try {
            portfolioService.sellAsset(user.getId(), symbol, quantity);
            return String.format("✅ Sold %s %s successfully!", quantity, symbol);
        } catch (Exception ex) {
            logger.error("Sell failed for {} {}: {}", symbol, quantity, ex.getMessage());
            return "❌ Sell failed: " + ex.getMessage();
        }
    }
}
