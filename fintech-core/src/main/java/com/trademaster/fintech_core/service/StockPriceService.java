package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.dto.PriceQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StockPriceService {

    private static final String STOOQ_URL = "https://stooq.com/q/l/?s=%s&i=d";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches a stock price from Stooq.
     * If the symbol is not supported, it returns empty.
     */
    public Optional<PriceQuote> getStockQuote(String symbol) {
        String normalized = symbol.toLowerCase();
        String stooqSymbol = normalized.contains(".") ? normalized : normalized + ".us";

        String url = String.format(STOOQ_URL, stooqSymbol);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return Optional.empty();
            }

            String[] lines = body.split("\n");
            if (lines.length < 2) {
                return Optional.empty();
            }

            String[] columns = lines[1].split(",");
            if (columns.length < 7) {
                return Optional.empty();
            }

            String closeValue = columns[6];
            if ("N/A".equalsIgnoreCase(closeValue)) {
                return Optional.empty();
            }

            BigDecimal price = new BigDecimal(closeValue);
            return Optional.of(new PriceQuote(symbol, price, "USD", "STOOQ", Instant.now()));
        } catch (Exception ex) {
            throw new PriceProviderException("Failed to fetch stock price.", ex);
        }
    }
}
