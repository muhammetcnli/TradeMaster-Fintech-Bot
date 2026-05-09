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
            return "❌ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Usage:") + "\n" +
                   com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/autobuy <symbol> <targetPrice> <quantity>");
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
                                        com.trademaster.fintech_core.entity.AutoTradeRule.TradeAction.BUY);
            
            return "🤖 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Auto-Buy Set!") + "\n" +
                   "Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(symbol) + "\n" +
                   "Target Price: " + targetPrice + "\n" +
                   "Quantity: " + quantity;
        } catch (Exception ex) {
            return "❌ Error: " + ex.getMessage();
        }
    }
}
