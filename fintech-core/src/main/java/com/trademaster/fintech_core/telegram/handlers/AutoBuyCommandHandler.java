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
 * Handler for /autobuy command.
 * Sets an automatic buy rule when price drops to target.
 */
@Component
@RequiredArgsConstructor
public class AutoBuyCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutoBuyCommandHandler.class);
    private final AlertService alertService;

    @Override
    public String getCommand() {
        return "/autobuy";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 3) {
            return "Usage: /autobuy <symbol> <targetPrice> <quantity>\nExample: /autobuy BTC 70000 0.01";
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

        logger.debug("AutoBuy set for user {}: {} {} at {}", user.getUsername(), symbol, quantity, targetPrice);

        alertService.addAutoTradeRule(user.getId(), symbol, targetPrice, quantity, TradeAction.BUY);
        return String.format("🤖 Auto BUY set: %s when price <= $%s, qty=%s", symbol, targetPrice, quantity);
    }
}
