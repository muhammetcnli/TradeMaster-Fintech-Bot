package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.service.AlertService.TriggerDirection;
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

    @Override
    public String getCommand() {
        return "/alert";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 2) {
            return "Usage: /alert <symbol> <targetPrice> [UP|DOWN]\nExample: /alert BTC 80000 UP";
        }

        String symbol = args[0].trim().toUpperCase();

        BigDecimal targetPrice;
        try {
            targetPrice = new BigDecimal(args[1]);
        } catch (NumberFormatException ex) {
            return "❌ Invalid target price: " + args[1];
        }

        TriggerDirection direction = TriggerDirection.UP;
        if (args.length >= 3) {
            try {
                direction = TriggerDirection.valueOf(args[2].trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return "❌ Invalid direction. Use UP or DOWN.";
            }
        }

        logger.debug("Alert set for user {}: {} {} {}", user.getUsername(), symbol, direction, targetPrice);

        alertService.addAlert(user.getId(), symbol, targetPrice, direction);
        return String.format("🔔 Alert set: %s %s $%s", symbol, direction, targetPrice);
    }
}
