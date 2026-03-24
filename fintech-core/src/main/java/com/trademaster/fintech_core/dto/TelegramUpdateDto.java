package com.trademaster.fintech_core.dto;

import lombok.Data;

@Data
public class TelegramUpdateDto {
    private Message message;

    @Data
    public static class Message {
        private Chat chat;
        private From from;
        private String text;
    }

    @Data
    public static class Chat {
        private Long id;
    }

    @Data
    public static class From {
        private String username;
    }
}


