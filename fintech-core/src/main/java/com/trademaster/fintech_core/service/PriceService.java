package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.dto.PriceQuote;

import java.util.Optional;

public interface PriceService {

    /**
     * Returns the latest price for a symbol.
     * If the symbol is not supported, it returns empty.
     */
    Optional<PriceQuote> getLatestPrice(String symbol);
}
