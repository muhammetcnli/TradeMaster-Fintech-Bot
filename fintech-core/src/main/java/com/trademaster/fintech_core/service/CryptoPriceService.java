package com.trademaster.fintech_core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.fintech_core.domain.dto.PriceQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CryptoPriceService {

    private static final String COINGECKO_URL = "https://api.coingecko.com/api/v3/simple/price?ids=%s&vs_currencies=usd";
    private static final String COINGECKO_SEARCH_URL = "https://api.coingecko.com/api/v3/search?query=%s";

    private static final Map<String, String> SYMBOL_TO_ID = Map.of(
            "BTC", "bitcoin",
            "ETH", "ethereum",
            "SOL", "solana",
            "XRP", "ripple",
            "ADA", "cardano",
            "DOGE", "dogecoin"
    );

    private static final Map<String, String> SYMBOL_CACHE = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches a crypto price from CoinGecko.
     * If the symbol is not supported, it returns empty.
     */
    public Optional<PriceQuote> getCryptoQuote(String symbol) {
        String id = resolveCoinGeckoId(symbol);
        if (id == null) {
            return Optional.empty();
        }

        String url = String.format(COINGECKO_URL, id);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode priceNode = root.path(id).path("usd");
            if (!priceNode.isNumber()) {
                return Optional.empty();
            }

            BigDecimal price = priceNode.decimalValue();
            return Optional.of(new PriceQuote(symbol, price, "USD", "COINGECKO", Instant.now()));
        } catch (Exception ex) {
            throw new PriceProviderException("Failed to fetch crypto price.", ex);
        }
    }

    /**
     * Resolves a CoinGecko coin id for a symbol.
     * It uses a small static map and a search fallback with cache.
     */
    private String resolveCoinGeckoId(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }

        String normalized = symbol.trim().toUpperCase();
        if (SYMBOL_TO_ID.containsKey(normalized)) {
            return SYMBOL_TO_ID.get(normalized);
        }

        if (SYMBOL_CACHE.containsKey(normalized)) {
            return SYMBOL_CACHE.get(normalized);
        }

        String url = String.format(COINGECKO_SEARCH_URL, normalized);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode coins = root.path("coins");
            if (!coins.isArray()) {
                return null;
            }

            for (JsonNode coin : coins) {
                String coinSymbol = coin.path("symbol").asText("").toUpperCase();
                if (normalized.equals(coinSymbol)) {
                    String id = coin.path("id").asText(null);
                    if (id != null && !id.isBlank()) {
                        SYMBOL_CACHE.put(normalized, id);
                        return id;
                    }
                }
            }

            return null;
        } catch (Exception ex) {
            throw new PriceProviderException("Failed to resolve coin id.", ex);
        }
    }
}
