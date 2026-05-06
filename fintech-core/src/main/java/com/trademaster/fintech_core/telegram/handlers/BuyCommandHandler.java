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
 * Handler for /buy command.
 * Buys a specified quantity of an asset.
 */
@Component
@RequiredArgsConstructor
public class BuyCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(BuyCommandHandler.class);
    private final PortfolioService portfolioService;

    @Override
    public String getCommand() {
        return "/buy";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 2) {
            return "Usage: /buy <symbol> <quantity>\nExample: /buy BTC 0.1";
        }

        String symbol = args[0].trim().toUpperCase();
        BigDecimal quantity;
        try {
            quantity = new BigDecimal(args[1]);
        } catch (NumberFormatException ex) {
            return "❌ Invalid quantity: " + args[1];
        }

        logger.debug("Buy {} {} for user {}", quantity, symbol, user.getUsername());

        try {
            portfolioService.buyAsset(user.getId(), symbol, quantity);
            return String.format("✅ Bought %s %s successfully!", quantity, symbol);
        } catch (Exception ex) {
            logger.error("Buy failed for {} {}: {}", symbol, quantity, ex.getMessage());
            return "❌ Buy failed: " + ex.getMessage();
        }
    }
}
