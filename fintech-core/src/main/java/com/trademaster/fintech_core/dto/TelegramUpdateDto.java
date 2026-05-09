package com.trademaster.fintech_core.dto;

import lombok.Data;
import java.util.List;

@Data
public class TelegramUpdateDto {
    private Long updateId;
    private Message message;
    private CallbackQuery callbackQuery;

    @Data
    public static class Message {
        private Long messageId;
        private Chat chat;
        private From from;
        private String text;
        private List<MessageEntity> entities;
    }

    @Data
    public static class CallbackQuery {
        private String id;
        private From from;
        private Message message;
        private String data;
    }

    @Data
    public static class Chat {
        private Long id;
        private String type;
    }

    @Data
    public static class From {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
    }

    @Data
    public static class MessageEntity {
        private String type;
        private Integer offset;
        private Integer length;
    }
}


