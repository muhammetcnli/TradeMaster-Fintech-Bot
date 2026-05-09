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
            return "❌ " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Usage:") + " " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/price <symbol> [CRYPTO|STOCK|FIAT]") + 
                   "\n\nExample: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("/price BTC");
        }

        String symbol = args[0].trim().toUpperCase();

        com.trademaster.fintech_core.dto.AssetType assetType = null;
        if (args.length >= 2) {
            try {
                assetType = com.trademaster.fintech_core.dto.AssetType.valueOf(args[1].trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return "⚠️ Invalid asset type: " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(args[1]) + ". Use CRYPTO, STOCK, or FIAT.";
            }
        }

        logger.info("Price check: {} by user {}", symbol, user.getUsername());

        MarketPriceDto price = assetType != null
                ? marketDataService.getCurrentPrice(symbol, assetType)
                : marketDataService.getCurrentPrice(symbol);

        StringBuilder sb = new StringBuilder();
        sb.append("💰 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold(symbol)).append(" Price\n\n")
          .append("💵 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold(price.getPrice().toString())).append(" ").append(price.getCurrency()).append("\n")
          .append("📊 Type: ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.italic(price.getAssetType().toString())).append("\n")
          .append("🏢 Provider: ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(price.getProvider()));

        return sb.toString();
    }
}
