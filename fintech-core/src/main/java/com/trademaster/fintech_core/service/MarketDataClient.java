package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;

public interface MarketDataClient {
    boolean supports(AssetType assetType);

    MarketPriceDto getCurrentPrice(String symbol, AssetType assetType);
}
