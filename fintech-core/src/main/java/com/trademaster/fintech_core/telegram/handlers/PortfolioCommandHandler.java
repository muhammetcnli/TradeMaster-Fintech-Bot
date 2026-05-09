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
        logger.info("Fetching portfolio for user: {}", user.getUsername());

        PortfolioDto portfolio = portfolioService.getUserPortfolio(user.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("📊 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Portfolio Summary")).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("💵 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Balance:")).append(" $").append(portfolio.getCurrentBalance()).append("\n");
        sb.append("📈 ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Total Value:")).append(" $").append(portfolio.getTotalPortfolioValue()).append("\n");
        sb.append("🗂 Assets: ").append(portfolio.getAssets().size()).append("\n");

        if (!portfolio.getAssets().isEmpty()) {
            sb.append("\n").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.italic("Holdings:")).append("\n");
            portfolio.getAssets().forEach(asset ->
                    sb.append("  • ").append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold(asset.getSymbol())).append(": ")
                      .append(com.trademaster.fintech_core.telegram.util.TelegramFormatter.code(asset.getQuantity().toString()))
                      .append(" @ $").append(asset.getCurrentPrice())
                      .append(" (PnL: ").append(asset.getProfitLossPercentage()).append("%)\n")
            );
        } else {
            sb.append("\n_No assets found in your portfolio._");
        }

        return sb.toString().trim();
    }
}
