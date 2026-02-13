package com.trademaster.fintech_core.domain.dto;

public record PriceResponse(
        String status,
        PriceQuote data
) {
}
