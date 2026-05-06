package com.trademaster.fintech_core.telegram.handlers;

import com.trademaster.fintech_core.dto.TelegramUpdateDto;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.telegram.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements CommandHandler {

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public String handle(User user, TelegramUpdateDto update, String[] args) {
        return """
                🚀 TradeMaster Bot Commands
                ━━━━━━━━━━━━━━━━━
                /start - Link account & get REST token
                /help - Show this message
                
                📈 Market Data (Auto-detects Crypto/Stock/Fiat)
                /price <symbol> - e.g. /price BTC, /price AAPL, /price PLN
                
                💰 Trading & Portfolio
                /portfolio - View your balance and assets
                /buy <symbol> <qty> - Buy asset (e.g. /buy BTC 0.1, /buy PLN 100)
                /sell <symbol> <qty> - Sell asset
                /watch <symbol> - Add to watchlist
                /watchlist - View your watchlist
                
                🤖 Automation & Rules
                /alert <symbol> <price> [UP|DOWN] - Price alerts
                /autobuy <symbol> <price> <qty> - Auto-execute buy
                /autosell <symbol> <price> <qty> - Auto-execute sell
                /rules - View all active alerts and rules
                """;
    }
}
