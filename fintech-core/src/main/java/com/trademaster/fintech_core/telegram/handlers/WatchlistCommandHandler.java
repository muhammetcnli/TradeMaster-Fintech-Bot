package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.dto.WatchListDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.service.PortfolioService;
import com.trademaster.fintech_core.telegram.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handler for /watchlist command.
 * Displays all watched assets with current prices.
 */
@Component
@RequiredArgsConstructor
public class WatchlistCommandHandler implements CommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(WatchlistCommandHandler.class);
    private final PortfolioService portfolioService;

    @Override
    public String getCommand() {
        return "/watchlist";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        logger.debug("Fetching watchlist for user: {}", user.getUsername());

        try {
            List<WatchListDto> watchList = portfolioService.getWatchList(user.getId());

            if (watchList.isEmpty()) {
                return "📋 Watchlist is empty.\nUse /watch <symbol> to add assets.";
            }

            StringBuilder sb = new StringBuilder("📋 Watchlist\n━━━━━━━━━━━━\n");
            for (WatchListDto item : watchList) {
                sb.append(String.format("  • %s: $%s\n", item.getSymbol(), item.getCurrentPrice()));
            }
            return sb.toString().trim();
        } catch (Exception ex) {
            logger.error("Error fetching watchlist for {}: {}", user.getUsername(), ex.getMessage());
            return "❌ Error fetching watchlist: " + ex.getMessage();
        }
    }
}
