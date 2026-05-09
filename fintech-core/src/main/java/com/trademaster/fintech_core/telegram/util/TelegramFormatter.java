package com.trademaster.fintech_core.telegram.util;

/**
 * Utility for formatting Telegram messages using MarkdownV2.
 * Handles character escaping to prevent API errors.
 */
public class TelegramFormatter {

    /**
     * Escapes special characters for Telegram HTML.
     * Special characters are: <, >, &
     */
    public static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String bold(String text) {
        return "<b>" + escapeHtml(text) + "</b>";
    }

    public static String italic(String text) {
        return "<i>" + escapeHtml(text) + "</i>";
    }

    public static String code(String text) {
        return "<code>" + escapeHtml(text) + "</code>";
    }

    public static String codeBlock(String language, String text) {
        return "<pre>" + escapeHtml(text) + "</pre>";
    }
    
    public static String link(String text, String url) {
        return "<a href=\"" + url + "\">" + escapeHtml(text) + "</a>";
    }
}
