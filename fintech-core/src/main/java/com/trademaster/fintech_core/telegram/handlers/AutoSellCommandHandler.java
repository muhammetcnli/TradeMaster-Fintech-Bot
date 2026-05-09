package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
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
            return "❌ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Usage:") + "\n" +
                   com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/autosell <symbol> <targetPrice> <quantity>");
        }

        String symbol = args[0].trim().toUpperCase();
        String priceStr = args[1].trim();
        String qtyStr = args[2].trim();

        try {
            com.trademaster.fintech_core.telegram.util.TelegramValidator.validateSymbol(symbol);
            com.trademaster.fintech_core.telegram.util.TelegramValidator.validateNumeric(priceStr, "Price");
            com.trademaster.fintech_core.telegram.util.TelegramValidator.validateNumeric(qtyStr, "Quantity");
            
            BigDecimal targetPrice = new BigDecimal(priceStr);
            BigDecimal quantity = new BigDecimal(qtyStr);

            alertService.addAutoTradeRule(user.getId(), symbol, targetPrice, quantity, 
                                        com.trademaster.fintech_core.entity.AutoTradeRule.TradeAction.SELL);
            
            return "🤖 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Auto-Sell Set!") + "\n" +
                   "Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(symbol) + "\n" +
                   "Target Price: " + targetPrice + "\n" +
                   "Quantity: " + quantity;
        } catch (Exception ex) {
            return "❌ Error: " + ex.getMessage();
        }
    }
}
