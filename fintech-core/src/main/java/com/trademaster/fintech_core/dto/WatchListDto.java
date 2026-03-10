package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WatchListDto {
    private String symbol;
    private BigDecimal currentPrice;
    // TargetPrice for now. TODO
    private BigDecimal targetPrice;
    private boolean isAlertEnabled;
    // last 24h up/down
    private String trend;
}
