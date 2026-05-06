package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.service.AlertService.PriceAlert;
import com.trademaster.fintech_core.service.AlertService.AutoTradeRule;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

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
        Map<String, PriceAlert> alerts = alertService.getUserAlerts(user.getId());
        Map<String, AutoTradeRule> rules = alertService.getUserAutoTradeRules(user.getId());

        if (alerts.isEmpty() && rules.isEmpty()) {
            return "No alerts or auto-trade rules set.";
        }

        StringBuilder sb = new StringBuilder("Active Rules\n");

        alerts.values().forEach(a ->
                sb.append("ALERT ").append(a.getSymbol()).append(" ")
                  .append(a.getDirection()).append(" ").append(a.getTargetPrice())
                  .append(a.isTriggered() ? " [triggered]" : "").append("\n"));

        rules.values().forEach(r ->
                sb.append("AUTO ").append(r.getAction()).append(" ")
                  .append(r.getSymbol()).append(" target=").append(r.getTargetPrice())
                  .append(" qty=").append(r.getQuantity())
                  .append(" active=").append(r.isActive()).append("\n"));

        return sb.toString().trim();
    }
}
