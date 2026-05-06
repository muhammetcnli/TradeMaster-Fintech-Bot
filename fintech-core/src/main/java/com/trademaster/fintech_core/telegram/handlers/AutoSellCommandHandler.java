package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.service.AlertService.TradeAction;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler for /autosell command.
 * Sets an automatic sell rule when price rises to target.
 */
@Component
@RequiredArgsConstructor
public class AutoSellCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutoSellCommandHandler.class);
    private final AlertService alertService;

    @Override
    public String getCommand() {
        return "/autosell";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 3) {
            return "Usage: /autosell <symbol> <targetPrice> <quantity>\nExample: /autosell BTC 90000 0.01";
        }

        String symbol = args[0].trim().toUpperCase();

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(args[1]);
        } catch (NumberFormatException ex) {
            return "❌ Invalid target price: " + args[1];
        }

        BigDecimal quantity;
        try {
            quantity = new BigDecimal(args[2]);
        } catch (NumberFormatException ex) {
            return "❌ Invalid quantity: " + args[2];
        }

        logger.debug("AutoSell set for user {}: {} {} at {}", user.getUsername(), symbol, quantity, targetPrice);

        alertService.addAutoTradeRule(user.getId(), symbol, targetPrice, quantity, TradeAction.SELL);
        return String.format("🤖 Auto SELL set: %s when price >= $%s, qty=%s", symbol, targetPrice, quantity);
    }
}
