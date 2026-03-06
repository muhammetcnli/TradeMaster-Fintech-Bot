package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.dto.PriceQuote;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Profile("mock")
public class MockPriceService implements PriceService {

    private static final Map<String, BigDecimal> PRICES = new HashMap<>();
    private static final Map<String, String> CURRENCIES = new HashMap<>();

    static {
        PRICES.put("BTC", new BigDecimal("67000.00"));
        CURRENCIES.put("BTC", "USD");

        PRICES.put("ETH", new BigDecimal("3500.00"));
        CURRENCIES.put("ETH", "USD");

        PRICES.put("EUR-PLN", new BigDecimal("4.35"));
        CURRENCIES.put("EUR-PLN", "PLN");

        PRICES.put("USD-TRY", new BigDecimal("32.50"));
        CURRENCIES.put("USD-TRY", "TRY");

        PRICES.put("AAPL", new BigDecimal("185.10"));
        CURRENCIES.put("AAPL", "USD");
    }

    /**
     * Returns a mock price for a symbol.
     * It returns empty when the symbol is not mapped.
     */
    @Override
    public Optional<PriceQuote> getLatestPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String key = symbol.trim().toUpperCase();
        BigDecimal price = PRICES.get(key);
        if (price == null) {
            return Optional.empty();
        }

        String currency = CURRENCIES.getOrDefault(key, "USD");
        return Optional.of(new PriceQuote(key, price, currency, "MOCK", Instant.now()));
    }
}
