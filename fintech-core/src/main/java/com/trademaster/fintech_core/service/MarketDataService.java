package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketDataService {

    private final List<MarketDataClient> marketDataClients;

    @Value("${market.default-asset-type:CRYPTO}")
    private String defaultAssetType;

    public MarketDataService(List<MarketDataClient> marketDataClients) {
        this.marketDataClients = marketDataClients;
    }

    public MarketPriceDto getCurrentPrice(String symbol) {
        return getCurrentPrice(symbol, AssetType.valueOf(defaultAssetType.toUpperCase()));
    }

    public MarketPriceDto getCurrentPrice(String symbol, AssetType assetType) {
        return marketDataClients.stream()
                .filter(client -> client.supports(assetType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider found for asset type: " + assetType))
                .getCurrentPrice(symbol, assetType);
    }
}