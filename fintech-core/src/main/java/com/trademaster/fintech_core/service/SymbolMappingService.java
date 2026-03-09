package com.trademaster.fintech_core.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SymbolMappingService {

    private static final Map<String, String> COINGECKO_IDS = Map.of(
            "BTC", "bitcoin",
            "ETH", "ethereum",
            "USDT", "tether",
            "BNB", "binancecoin",
            "SOL", "solana",
            "XRP", "ripple",
            "ADA", "cardano"
    );

    public String toCoinGeckoId(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }

        return COINGECKO_IDS.getOrDefault(symbol.toUpperCase(), symbol.toLowerCase());
    }
}
