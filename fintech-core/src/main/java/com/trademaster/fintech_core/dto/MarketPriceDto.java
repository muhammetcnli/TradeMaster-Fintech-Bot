package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class MarketPriceDto {
    private String symbol;
    private BigDecimal price;
    private String currency;
    private String provider;
    private String assetType;
    private Instant timestamp;
}
