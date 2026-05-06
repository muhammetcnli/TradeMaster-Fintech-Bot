package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.MarketPriceDto;
import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.MarketDataService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for /price command.
 * Fetches current market price for a given symbol.
 */
@Component
@RequiredArgsConstructor
public class PriceCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PriceCommandHandler.class);
    private final MarketDataService marketDataService;

    @Override
    public String getCommand() {
        return "/price";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        if (args.length < 1) {
            return "Usage: /price <symbol> [CRYPTO|STOCK|FIAT]\nExamples:\n  /price BTC\n  /price AAPL STOCK\n  /price PLN\n  /price USDTRY FIAT";
        }

        String symbol = args[0].trim().toUpperCase();

        // Optional explicit asset type override
        com.trademaster.fintech_core.dto.AssetType assetType = null;
        if (args.length >= 2) {
            try {
                assetType = com.trademaster.fintech_core.dto.AssetType.valueOf(args[1].trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return "❌ Invalid asset type: " + args[1] + ". Use CRYPTO, STOCK, or FIAT.";
            }
        }

        logger.debug("Fetching price for {} (type: {}, user: {})", symbol,
                assetType != null ? assetType : "auto-detect", user.getUsername());

        try {
            MarketPriceDto price = assetType != null
                    ? marketDataService.getCurrentPrice(symbol, assetType)
                    : marketDataService.getCurrentPrice(symbol);

            return String.format("💰 %s = %s %s\n📊 Type: %s | Provider: %s",
                    symbol, price.getPrice(), price.getCurrency(),
                    price.getAssetType(), price.getProvider());
        } catch (Exception ex) {
            logger.error("Error fetching price for {}: {}", symbol, ex.getMessage());
            return "❌ Could not fetch price for " + symbol + ": " + ex.getMessage();
        }
    }
}
