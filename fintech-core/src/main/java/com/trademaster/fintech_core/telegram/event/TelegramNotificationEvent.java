package com.trademaster.fintech_core.telegram.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event triggered when a price alert or auto-trade is hit.
 */
@Getter
public class TelegramNotificationEvent extends ApplicationEvent {
    private final Long telegramChatId;
    private final String message;

    public TelegramNotificationEvent(Object source, Long telegramChatId, String message) {
        super(source);
        this.telegramChatId = telegramChatId;
        this.message = message;
    }
}
