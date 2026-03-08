package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import com.trademaster.fintech_core.service.MarketDataService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/market")
public class MarketController {
    private final MarketDataService marketDataService;

    public MarketController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/price/{symbol}")
    public MarketPriceDto getPrice(@PathVariable String symbol,
                                   @RequestParam(required = false) AssetType assetType) {
        if (assetType == null) {
            return marketDataService.getCurrentPrice(symbol);
        }
        return marketDataService.getCurrentPrice(symbol, assetType);
    }
}
