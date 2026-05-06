package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.PortfolioService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for /portfolio command.
 * Displays user's current portfolio with balance and asset values.
 */
@Component
@RequiredArgsConstructor
public class PortfolioCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioCommandHandler.class);
    private final PortfolioService portfolioService;

    @Override
    public String getCommand() {
        return "/portfolio";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        logger.debug("Fetching portfolio for user: {}", user.getUsername());

        try {
            PortfolioDto portfolio = portfolioService.getUserPortfolio(user.getId());

            StringBuilder sb = new StringBuilder();
            sb.append("📊 Portfolio Summary\n");
            sb.append("━━━━━━━━━━━━━━━━━\n");
            sb.append(String.format("💵 Balance: $%s\n", portfolio.getCurrentBalance()));
            sb.append(String.format("📈 Total Value: $%s\n", portfolio.getTotalPortfolioValue()));
            sb.append(String.format("🗂 Assets: %d\n", portfolio.getAssets().size()));

            if (!portfolio.getAssets().isEmpty()) {
                sb.append("\nHoldings:\n");
                portfolio.getAssets().forEach(asset ->
                        sb.append(String.format("  • %s: %s @ $%s (PnL: %s%%)\n",
                                asset.getSymbol(),
                                asset.getQuantity(),
                                asset.getCurrentPrice(),
                                asset.getProfitLossPercentage()))
                );
            }

            return sb.toString().trim();
        } catch (Exception ex) {
            logger.error("Error fetching portfolio for {}: {}", user.getUsername(), ex.getMessage());
            return "❌ Error fetching portfolio: " + ex.getMessage();
        }
    }
}
