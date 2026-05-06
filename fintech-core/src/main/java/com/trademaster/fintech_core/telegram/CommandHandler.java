package com.trademaster.fintech_core.telegram;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;

/**
 * Interface for handling Telegram commands.
 * Each command (e.g., /start, /balance, /portfolio) implements this.
 */
public interface CommandHandler {

    /**
     * The command name this handler responds to (e.g., "/start")
     */
    String getCommand();

    /**
     * Handle the command execution.
     * @param user The authenticated Telegram user
     * @param update The full Telegram update
     * @param args Command arguments after the command itself
     * @return Response message to send to Telegram chat
     */
    String handle(User user, TelegramUpdateDto update, String[] args);
}

