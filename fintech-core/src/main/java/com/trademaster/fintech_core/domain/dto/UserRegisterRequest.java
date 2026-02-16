package com.trademaster.fintech_core.domain.dto;

public record UserRegisterRequest(
        Long chatId,
        String username,
        String firstName,
        String lastName
) {
}
