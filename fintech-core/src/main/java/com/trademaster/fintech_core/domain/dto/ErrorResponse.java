package com.trademaster.fintech_core.domain.dto;

public record ErrorResponse(
        String status,
        String error,
        String message,
        String timestamp,
        String path
) {
}
