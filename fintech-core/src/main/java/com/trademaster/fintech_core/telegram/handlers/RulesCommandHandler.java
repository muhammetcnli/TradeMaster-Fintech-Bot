package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RulesCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(RulesCommandHandler.class);
    private final AlertService alertService;

    @Override
    public String getCommand() {
        return "/rules";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        var alerts = alertService.getActiveAlerts(user.getId());
        var rules = alertService.getActiveAutoTradeRules(user.getId());

        if (alerts.isEmpty() && rules.isEmpty()) {
            return "📭 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.italic("No active alerts or auto-trade rules found.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Your Active Rules")).append("\n\n");

        if (!alerts.isEmpty()) {
            sb.append("🔔 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Price Alerts")).append("\n");
            for (var alert : alerts) {
                sb.append("• ").append(alert.getSymbol()).append(": ")
                  .append(alert.getType().toString().replace("PRICE_", ""))
                  .append(" ").append(alert.getTargetValue()).append("\n");
            }
            sb.append("\n");
        }

        if (!rules.isEmpty()) {
            sb.append("🤖 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Auto-Trades")).append("\n");
            for (var rule : rules) {
                sb.append("• ").append(rule.getAction()).append(" ")
                  .append(rule.getSymbol()).append(" at ").append(rule.getTargetPrice())
                  .append(" (Qty: ").append(rule.getQuantity()).append(")\n");
            }
        }

        return sb.toString().trim();
    }
}
