package com.trademaster.fintech_core.telegram.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating Telegram keyboards.
 */
public class KeyboardFactory {

    /**
     * Creates a persistent Reply Keyboard with the given buttons.
     * 
     * @param buttonRows List of button rows, where each row is a list of button labels.
     * @return Map representing the reply_markup
     */
    public static Map<String, Object> createReplyKeyboard(List<List<String>> buttonRows) {
        List<List<Map<String, Object>>> keyboard = new ArrayList<>();

        for (List<String> row : buttonRows) {
            List<Map<String, Object>> keyboardRow = new ArrayList<>();
            for (String label : row) {
                Map<String, Object> button = new HashMap<>();
                button.put("text", label);
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
        }

        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("keyboard", keyboard);
        replyMarkup.put("resize_keyboard", true);
        replyMarkup.put("one_time_keyboard", false);
        return replyMarkup;
    }

    /**
     * Creates an Inline Keyboard with the given buttons and callbacks.
     * 
     * @param buttonRows List of button rows, where each row is a Map of label to callbackData.
     * @return Map representing the reply_markup
     */
    public static Map<String, Object> createInlineKeyboard(List<Map<String, String>> buttonRows) {
        List<List<Map<String, Object>>> keyboard = new ArrayList<>();

        for (Map<String, String> row : buttonRows) {
            List<Map<String, Object>> keyboardRow = new ArrayList<>();
            for (Map.Entry<String, String> entry : row.entrySet()) {
                Map<String, Object> button = new HashMap<>();
                button.put("text", entry.getKey());
                button.put("callback_data", entry.getValue());
                keyboardRow.add(button);
            }
            keyboard.add(keyboardRow);
        }

        Map<String, Object> replyMarkup = new HashMap<>();
        replyMarkup.put("inline_keyboard", keyboard);
        return replyMarkup;
    }

    /**
     * Helper to create the main menu keyboard.
     */
    public static Map<String, Object> mainMenu() {
        return createReplyKeyboard(List.of(
            List.of("💰 Price", "📊 Portfolio"),
            List.of("🔔 Alerts", "👀 Watchlist"),
            List.of("❓ Help")
        ));
    }
}
