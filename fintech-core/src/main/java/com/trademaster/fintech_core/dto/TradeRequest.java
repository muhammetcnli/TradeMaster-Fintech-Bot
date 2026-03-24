package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class TradeRequest {
    private UUID userId;
    private BigDecimal quantity;
}

