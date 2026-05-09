package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.service.MarketDataService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Handler for /alert command.
 * Sets a price alert for a given symbol.
 */
@Component
@RequiredArgsConstructor
public class AlertCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(AlertCommandHandler.class);
    private final AlertService alertService;
    private final MarketDataService marketDataService;

    @Override
    public String getCommand() {
        return "/alert";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 2) {
            return "❌ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Usage:") + "\n" +
                   "Price Alert: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/alert BTC 80000 [UP|DOWN]") + "\n" +
                   "Percentage Alert: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/alert BTC +5%") + " or " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/alert BTC -10%");
        }

        String symbol = args[0].trim().toUpperCase();
        String target = args[1].trim();

        try {
            com.trademaster.fintech_core.telegram.util.TelegramValidator.validateSymbol(symbol);
            
            if (target.endsWith("%")) {
                // Percentage alert
                com.trademaster.fintech_core.telegram.util.TelegramValidator.validatePercentage(target);
                BigDecimal percent = new BigDecimal(target.replace("%", ""));
                alertService.addPercentageAlert(user.getId(), symbol, percent);
                return "✅ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Percentage alert set!") + "\n" +
                       "Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(symbol) + "\n" +
                       "Target: " + (percent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + percent + "% change";
            } else {
                // Price alert
                com.trademaster.fintech_core.telegram.util.TelegramValidator.validateNumeric(target, "Price");
                BigDecimal price = new BigDecimal(target);
                com.trademaster.fintech_core.entity.AlertType type = com.trademaster.fintech_core.entity.AlertType.PRICE_ABOVE;
                
                if (args.length >= 3) {
                    String dir = args[2].trim().toUpperCase();
                    if (dir.equals("DOWN") || dir.equals("BELOW")) {
                        type = com.trademaster.fintech_core.entity.AlertType.PRICE_BELOW;
                    }
                } else {
                    // Auto-detect direction based on current price
                    BigDecimal current = marketDataService.getCurrentPrice(symbol).getPrice();
                    if (price.compareTo(current) < 0) {
                        type = com.trademaster.fintech_core.entity.AlertType.PRICE_BELOW;
                    }
                }

                alertService.addPriceAlert(user.getId(), symbol, price, type);
                return "✅ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Price alert set!") + "\n" +
                       "Asset: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(symbol) + "\n" +
                       "Target: " + type.toString().replace("PRICE_", "") + " " + price;
            }
        } catch (NumberFormatException ex) {
            return "❌ Invalid number format: " + target;
        } catch (Exception ex) {
            logger.error("Error setting alert for {}: {}", symbol, ex.getMessage());
            return "❌ Error: " + ex.getMessage();
        }
    }
}
