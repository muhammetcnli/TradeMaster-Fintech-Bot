package com.trademaster.fintech_core.telegram.util;

/**
 * Utility for validating Telegram bot inputs.
 */
public class TelegramValidator {

    public static void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty.");
        }
    }

    public static void validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be empty.");
        }
        if (symbol.length() > 15) {
            throw new IllegalArgumentException("Symbol is too long.");
        }
    }

    public static void validateNumeric(String value, String fieldName) {
        try {
            new java.math.BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for " + fieldName + ": " + value);
        }
    }

    public static void validatePercentage(String value) {
        if (!value.endsWith("%")) {
            throw new IllegalArgumentException("Percentage must end with %");
        }
        try {
            new java.math.BigDecimal(value.replace("%", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid percentage format: " + value);
        }
    }
}
