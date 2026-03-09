package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.time.Instant;

@Service
public class CoinGeckoMarketDataClient implements MarketDataClient {

    private final RestTemplate restTemplate;
    private final SymbolMappingService symbolMappingService;

    @Value("${coingecko.api.base-url:https://api.coingecko.com/api/v3}")
    private String baseUrl;

    @Value("${coingecko.api.key:}")
    private String apiKey;

    public CoinGeckoMarketDataClient(RestTemplate restTemplate,
                                     SymbolMappingService symbolMappingService) {
        this.restTemplate = restTemplate;
        this.symbolMappingService = symbolMappingService;
    }

    @Override
    public boolean supports(AssetType assetType) {
        return AssetType.CRYPTO == assetType;
    }

    @Override
    public MarketPriceDto getCurrentPrice(String symbol, AssetType assetType) {
        String coinId = symbolMappingService.toCoinGeckoId(symbol);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/simple/price")
                .queryParam("ids", coinId)
                .queryParam("vs_currencies", "usd");

        if (apiKey != null && !apiKey.isBlank()) {
            builder.queryParam("x_cg_demo_api_key", apiKey);
        }

        try {
            JsonNode response = restTemplate.getForObject(builder.toUriString(), JsonNode.class);

            if (response != null && response.has(coinId) && response.get(coinId).has("usd")) {
                return MarketPriceDto.builder()
                        .symbol(symbol.toUpperCase())
                        .price(response.get(coinId).get("usd").decimalValue())
                        .currency("USD")
                        .provider("COINGECKO")
                        .assetType(assetType.name())
                        .timestamp(Instant.now())
                        .build();
            }
        } catch (Exception ex) {
            throw new RuntimeException("CoinGecko API error for symbol: " + symbol, ex);
        }

        throw new RuntimeException("Price not found for symbol: " + symbol);
    }
}
