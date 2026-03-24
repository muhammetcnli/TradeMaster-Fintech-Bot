package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram")
public class TelegramWebhookController {

    private final TelegramBotService telegramBotService;

    @Value("${telegram.webhook.secret:}")
    private String webhookSecret;

    public TelegramWebhookController(TelegramBotService telegramBotService) {
        this.telegramBotService = telegramBotService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody TelegramUpdateDto update,
                                              @RequestHeader(value = "X-Telegram-Bot-Api-Secret-Token", required = false)
                                              String secretToken) {
        String configuredSecret = webhookSecret == null ? "" : webhookSecret.trim();
        String providedSecret = secretToken == null ? "" : secretToken.trim();

        // If secret is configured, every Telegram webhook call must carry the same header value.
        if (!configuredSecret.isBlank() && !configuredSecret.equals(providedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        telegramBotService.handleUpdate(update);
        return ResponseEntity.ok().build();
    }
}

