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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForexPriceService {

    private static final String FRANKFURTER_URL = "https://api.frankfurter.app/latest?from=%s&to=%s";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetches a forex rate from Frankfurter.
     * If the symbol is not supported, it returns empty.
     */
    public Optional<PriceQuote> getForexQuote(String symbol) {
        String[] parts = symbol.split("-");
        if (parts.length != 2) {
            return Optional.empty();
        }

        String base = parts[0];
        String quote = parts[1];

        String url = String.format(FRANKFURTER_URL, base, quote);

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode rateNode = root.path("rates").path(quote);
            if (!rateNode.isNumber()) {
                return Optional.empty();
            }

            BigDecimal price = rateNode.decimalValue();
            return Optional.of(new PriceQuote(symbol, price, quote, "FRANKFURTER", Instant.now()));
        } catch (Exception ex) {
            throw new PriceProviderException("Failed to fetch forex price.", ex);
        }
    }
}
