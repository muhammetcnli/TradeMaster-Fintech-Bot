package com.trademaster.fintech_core.domain.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceQuote(
        String symbol,
        BigDecimal price,
        String currency,
        String source,
        Instant asOf
) {
}
