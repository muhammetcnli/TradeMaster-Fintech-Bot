package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AssetItemDto{
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal averageCost;
    private BigDecimal profitLossPercentage;

}
