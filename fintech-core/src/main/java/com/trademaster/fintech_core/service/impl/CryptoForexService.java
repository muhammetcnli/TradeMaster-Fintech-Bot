package com.trademaster.fintech_core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.fintech_core.service.PriceService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class CryptoForexService implements PriceService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // JSON'ı parçalamak için

    public CryptoForexService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public BigDecimal getLatestPrice(String symbol) {
        String upperSymbol = symbol.toUpperCase(Locale.ROOT);

        if (upperSymbol.contains("BTC") || upperSymbol.contains("ETH")) {
            return getCryptoPrice("bitcoin");
        } else if (upperSymbol.length() == 6) {

            String from = upperSymbol.substring(0, 3);
            String to = upperSymbol.substring(3);
            return getForexPrice(from, to);
        }

        return BigDecimal.ZERO;
    }

    // CoinGecko API Çağrısı
    private BigDecimal getCryptoPrice(String coinId) {
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinId + "&vs_currencies=usd";
            // Gelen JSON: {"bitcoin":{"usd":95432.10}}
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(jsonResponse);
            double price = root.path(coinId).path("usd").asDouble();

            return BigDecimal.valueOf(price);
        } catch (Exception e) {
            System.err.println("Crypto API Hatası: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // Frankfurter API Çağrısı (Forex)
    private BigDecimal getForexPrice(String from, String to) {
        try {
            String url = "https://api.frankfurter.app/latest?from=" + from + "&to=" + to;
            // Gelen JSON: {"amount":1.0,"base":"EUR","date":"...","rates":{"PLN":4.34}}
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JsonNode root = objectMapper.readTree(jsonResponse);
            double price = root.path("rates").path(to).asDouble();

            return BigDecimal.valueOf(price);
        } catch (Exception e) {
            System.err.println("Forex API Hatası: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}