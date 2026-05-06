package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetType;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MarketDataService {

    private static final Logger logger = LoggerFactory.getLogger(MarketDataService.class);

    private final List<MarketDataClient> marketDataClients;

    @Value("${market.default-asset-type:CRYPTO}")
    private String defaultAssetType;

    /**
     * Well-known fiat currency codes (ISO 4217).
     * Used for auto-detection when no assetType is specified.
     */
    private static final Set<String> KNOWN_FIAT_SYMBOLS = Set.of(
            "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD",
            "TRY", "PLN", "SEK", "NOK", "DKK", "CZK", "HUF", "RON",
            "BGN", "HRK", "ISK", "RUB", "UAH", "BRL", "MXN", "ARS",
            "CLP", "COP", "PEN", "CNY", "INR", "KRW", "SGD", "HKD",
            "TWD", "THB", "MYR", "IDR", "PHP", "VND", "ZAR", "EGP",
            "NGN", "KES", "GHS", "MAD", "SAR", "AED", "QAR", "KWD",
            "BHD", "OMR", "JOD", "ILS"
    );

    /**
     * Well-known stock ticker patterns and common US stock symbols.
     * For robust detection, stocks are identified by NOT being crypto or fiat.
     */
    private static final Set<String> KNOWN_STOCK_SYMBOLS = Set.of(
            "AAPL", "MSFT", "GOOGL", "GOOG", "AMZN", "META", "TSLA", "NVDA",
            "AMD", "INTC", "NFLX", "DIS", "PYPL", "SQ", "SHOP", "UBER",
            "COIN", "HOOD", "PLTR", "SNOW", "CRM", "ORCL", "IBM", "CSCO",
            "QCOM", "AVGO", "TXN", "MU", "ADBE", "NOW"
    );

    /**
     * Well-known crypto symbols.
     */
    private static final Set<String> KNOWN_CRYPTO_SYMBOLS = Set.of(
            "BTC", "ETH", "USDT", "BNB", "SOL", "XRP", "ADA", "DOGE",
            "DOT", "AVAX", "LINK", "MATIC", "UNI", "LTC", "ATOM",
            "NEAR", "APT", "ARB", "OP", "FIL", "ICP", "SHIB", "PEPE"
    );

    public MarketDataService(List<MarketDataClient> marketDataClients) {
        this.marketDataClients = marketDataClients;
    }

    /**
     * Get price with auto-detection of asset type.
     * Tries to detect from the symbol, falls back to configured default.
     */
    public MarketPriceDto getCurrentPrice(String symbol) {
        AssetType detected = detectAssetType(symbol);
        logger.debug("Auto-detected asset type for {}: {}", symbol, detected);
        return getCurrentPrice(symbol, detected);
    }

    /**
     * Get price with explicit asset type.
     */
    public MarketPriceDto getCurrentPrice(String symbol, AssetType assetType) {
        return marketDataClients.stream()
                .filter(client -> client.supports(assetType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider found for asset type: " + assetType))
                .getCurrentPrice(symbol, assetType);
    }

    /**
     * Detect the asset type from a symbol.
     *
     * Detection logic:
     * 1. If symbol is 6 chars and both halves are known fiat codes → FIAT (e.g., USDTRY, EURPLN)
     * 2. If symbol is a known fiat code by itself → FIAT (paired with USD, e.g., PLN → PLNUSD)
     * 3. If symbol is a known stock ticker → STOCK
     * 4. If symbol is a known crypto → CRYPTO
     * 5. Fallback: configured default (CRYPTO)
     */
    public AssetType detectAssetType(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return AssetType.valueOf(defaultAssetType.toUpperCase());
        }

        String upper = symbol.toUpperCase().trim();

        // Check for 6-char fiat pair like USDTRY, EURPLN
        if (upper.length() == 6) {
            String left = upper.substring(0, 3);
            String right = upper.substring(3, 6);
            if (KNOWN_FIAT_SYMBOLS.contains(left) && KNOWN_FIAT_SYMBOLS.contains(right)) {
                return AssetType.FIAT;
            }
        }

        // Check for single fiat currency code (e.g., PLN, EUR, TRY)
        if (KNOWN_FIAT_SYMBOLS.contains(upper)) {
            return AssetType.FIAT;
        }

        // Check known crypto
        if (KNOWN_CRYPTO_SYMBOLS.contains(upper)) {
            return AssetType.CRYPTO;
        }

        // Check known stock
        if (KNOWN_STOCK_SYMBOLS.contains(upper)) {
            return AssetType.STOCK;
        }

        // Fallback to default
        return AssetType.valueOf(defaultAssetType.toUpperCase());
    }
}