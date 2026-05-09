package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.AlertService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import com.trademaster.fintech_core.telegram.util.TelegramFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteAlertCommandHandler implements CommandHandler {

    private final AlertService alertService;

    @Override
    public String getCommand() {
        return "/delalert";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 1) {
            return "❌ Please provide the alert ID.\nUsage: " + TelegramFormatter.code("/delalert <id>");
        }

        String partialId = args[0].trim();
        
        try {
            // Find the alert by partial ID (since we show only first 8 chars)
            var activeAlerts = alertService.getActiveAlerts(user.getId());
            UUID targetId = activeAlerts.stream()
                    .filter(a -> a.getId().toString().startsWith(partialId))
                    .map(com.trademaster.fintech_core.entity.Alert::getId)
                    .findFirst()
                    .orElse(null);

            if (targetId == null) {
                return "❌ Alert not found with ID starting with: " + partialId;
            }

            alertService.deleteAlert(user.getId(), targetId);
            return "✅ Alert deleted successfully.";
        } catch (Exception ex) {
            return "❌ Error: " + ex.getMessage();
        }
    }
}
