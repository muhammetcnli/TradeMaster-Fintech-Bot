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
public class FrankfurterMarketDataClient implements MarketDataClient {

    private final RestTemplate restTemplate;

    @Value("${frankfurter.api.base-url:https://api.frankfurter.dev/v1}")
    private String baseUrl;

    public FrankfurterMarketDataClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean supports(AssetType assetType) {
        return assetType == AssetType.FIAT;
    }

    @Override
    public MarketPriceDto getCurrentPrice(String symbol, AssetType assetType) {
        String normalized = normalizeFxSymbol(symbol);
        String fromCurrency = normalized.substring(0, 3);
        String toCurrency = normalized.substring(3, 6);

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/latest")
                .queryParam("base", fromCurrency)
                .queryParam("symbols", toCurrency)
                .toUriString();

        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response == null || response.isNull() || !response.has("rates")) {
                throw new RuntimeException("Empty response from Frankfurter for symbol: " + normalized);
            }

            JsonNode rates = response.get("rates");
            if (!rates.has(toCurrency)) {
                throw new RuntimeException("Frankfurter did not return a rate for symbol: " + normalized);
            }

            BigDecimal rate = new BigDecimal(rates.get(toCurrency).asString());

            return MarketPriceDto.builder()
                    .symbol(normalized)
                    .price(rate)
                    .currency(toCurrency)
                    .provider("FRANKFURTER")
                    .assetType(AssetType.FIAT.name())
                    .timestamp(Instant.now())
                    .build();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Frankfurter FX API error for symbol: " + normalized, ex);
        }
    }

    private String normalizeFxSymbol(String symbol) {
        String normalized = symbol.toUpperCase().replace("/", "").replace("-", "").trim();
        if (normalized.length() != 6) {
            throw new IllegalArgumentException("FIAT symbol must be in 6-letter format, e.g. USDTRY or EURUSD");
        }
        return normalized;
    }
}
