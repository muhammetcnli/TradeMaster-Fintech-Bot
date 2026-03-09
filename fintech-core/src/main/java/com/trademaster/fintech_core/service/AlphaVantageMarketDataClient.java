package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class AlphaVantageMarketDataClient implements MarketDataClient {

    private static final String STOCK_PRICE_FIELD = "05. price";

    private final RestTemplate restTemplate;

    @Value("${alphavantage.api.base-url:https://www.alphavantage.co/query}")
    private String baseUrl;

    @Value("${alphavantage.api.key:}")
    private String apiKey;

    public AlphaVantageMarketDataClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean supports(AssetType assetType) {
        return assetType == AssetType.STOCK;
    }

    @Override
    public MarketPriceDto getCurrentPrice(String symbol, AssetType assetType) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Alpha Vantage API key is missing");
        }

        if (assetType != AssetType.STOCK) {
            throw new IllegalArgumentException("Unsupported asset type for Alpha Vantage: " + assetType);
        }

        return fetchStockQuote(symbol);
    }

    private MarketPriceDto fetchStockQuote(String symbol) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("function", "GLOBAL_QUOTE")
                .queryParam("symbol", symbol.toUpperCase())
                .queryParam("apikey", apiKey)
                .toUriString();

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            ensureSuccessfulResponse(response, symbol);

            JsonNode quote = response.get("Global Quote");
            if (quote != null && quote.has(STOCK_PRICE_FIELD)) {
                return MarketPriceDto.builder()
                        .symbol(symbol.toUpperCase())
                        .price(readDecimal(quote, STOCK_PRICE_FIELD))
                        .currency("USD")
                        .provider("ALPHA_VANTAGE")
                        .assetType(AssetType.STOCK.name())
                        .timestamp(Instant.now())
                        .build();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Alpha Vantage stock API error for symbol: " + symbol, ex);
        }

        throw new RuntimeException("Stock price not found for symbol: " + symbol);
    }

    private void ensureSuccessfulResponse(JsonNode response, String symbol) {
        if (response == null || response.isNull()) {
            throw new RuntimeException("Empty response from Alpha Vantage for STOCK symbol: " + symbol);
        }

        if (response.has("Error Message")) {
            throw new RuntimeException("Alpha Vantage returned an error for " + symbol + ": " + response.get("Error Message").asString());
        }

        if (response.has("Information")) {
            throw new RuntimeException("Alpha Vantage info for " + symbol + ": " + response.get("Information").asString());
        }

        if (response.has("Note")) {
            throw new RuntimeException("Alpha Vantage rate limit reached for " + symbol + ": " + response.get("Note").asString());
        }
    }

    private BigDecimal readDecimal(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            throw new IllegalArgumentException("Missing numeric field: " + fieldName);
        }

        String rawValue = valueNode.asString();
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Blank numeric field: " + fieldName);
        }

        try {
            return new BigDecimal(rawValue.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid decimal value for field: " + fieldName + " -> " + rawValue, ex);
        }
    }
}
