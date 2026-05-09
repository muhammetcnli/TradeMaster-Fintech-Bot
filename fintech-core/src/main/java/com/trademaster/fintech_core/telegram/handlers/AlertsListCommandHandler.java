package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.Alert;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import com.trademaster.fintech_core.telegram.util.TelegramFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertsListCommandHandler implements CommandHandler {

    private final AlertService alertService;

    @Override
    public String getCommand() {
        return "/alerts";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        List<Alert> alerts = alertService.getActiveAlerts(user.getId());

        if (alerts.isEmpty()) {
            return "📭 You have no active alerts.\nUse " + TelegramFormatter.code("/alert <symbol> <price>") + " to create one.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔔 ").append(TelegramFormatter.bold("Your Active Alerts")).append("\n\n");

        for (Alert alert : alerts) {
            sb.append("🆔 ").append(TelegramFormatter.code(alert.getId().toString().substring(0, 8)))
              .append(" | ").append(TelegramFormatter.bold(alert.getSymbol()))
              .append("\n└ ");
            
            switch (alert.getType()) {
                case PRICE_ABOVE: sb.append("Above ").append(alert.getTargetValue()); break;
                case PRICE_BELOW: sb.append("Below ").append(alert.getTargetValue()); break;
                case PERCENT_CHANGE_UP: sb.append("Up ").append(alert.getTargetValue()).append("%"); break;
                case PERCENT_CHANGE_DOWN: sb.append("Down ").append(alert.getTargetValue()).append("%"); break;
            }
            sb.append("\n\n");
        }

        sb.append("To delete: ").append(TelegramFormatter.code("/delalert <ID>"));
        return sb.toString().trim();
    }
}
