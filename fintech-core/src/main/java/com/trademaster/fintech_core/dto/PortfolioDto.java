package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PortfolioDto {
    private UUID userId;
    private String username;
    private BigDecimal currentBalance;
    private BigDecimal totalPortfolioValue;

    private List<AssetItemDto> assets;
    private List<WatchListDto> watchlist;
}
