package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.dto.PriceQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Primary
@RequiredArgsConstructor
public class DefaultPriceService implements PriceService {

    private static final Pattern FOREX_PATTERN = Pattern.compile("^[A-Z]{3}-[A-Z]{3}$");

    private final CryptoPriceService cryptoPriceService;
    private final ForexPriceService forexPriceService;
    private final StockPriceService stockPriceService;
    private final PriceCacheService priceCacheService;

    /**
     * Routes the symbol to the correct provider.
     * It returns empty when the symbol is not supported.
     */
    @Override
    public Optional<PriceQuote> getLatestPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String normalized = symbol.trim().toUpperCase();
        Optional<PriceQuote> cached = priceCacheService.getPrice(normalized);
        if (cached.isPresent()) {
            return cached;
        }

        if (priceCacheService.isOnCooldown(normalized)) {
            throw new PriceProviderException("Upstream cooldown is active.", null);
        }

        try {
            Optional<PriceQuote> quote;
            if (FOREX_PATTERN.matcher(normalized).matches()) {
                quote = forexPriceService.getForexQuote(normalized);
            } else {
                quote = cryptoPriceService.getCryptoQuote(normalized);
                if (quote.isEmpty()) {
                    quote = stockPriceService.getStockQuote(normalized);
                }
            }

            quote.ifPresent(priceCacheService::putPrice);
            return quote;
        } catch (PriceProviderException ex) {
            priceCacheService.setCooldown(normalized);
            throw ex;
        }
    }
}
