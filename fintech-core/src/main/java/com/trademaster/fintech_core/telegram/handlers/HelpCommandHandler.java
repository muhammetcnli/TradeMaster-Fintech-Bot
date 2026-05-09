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
        return "🚀 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("TradeMaster Bot Commands") + "\n" +
               "━━━━━━━━━━━━━━━━━\n" +
               "/start - Link account & get main menu\n" +
               "/help - Show this message\n\n" +

               "📈 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Market Data") + "\n" +
               "/price " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol>") + " - Get current price\n\n" +

               "💰 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Trading & Portfolio") + "\n" +
               "/portfolio - View balance & holdings\n" +
               "/buy " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol> <qty>") + " - Buy asset\n" +
               "/sell " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol> <qty>") + " - Sell asset\n\n" +

               "🔔 " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.bold("Alerts & Automation") + "\n" +
               "/alert " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol> <target>") + " - Price or % alert\n" +
               "  └ Ex: /alert BTC 90000\n" +
               "  └ Ex: /alert ETH +5%\n" +
               "/alerts - List your active alerts\n" +
               "/delalert " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<id>") + " - Delete an alert\n" +
               "/autobuy " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol> <price> <qty>") + " - Auto buy\n" +
               "/autosell " + com.trademaster.fintech_core.telegram.util.TelegramFormatter.code("<symbol> <price> <qty>") + " - Auto sell\n" +
               "/rules - View all active rules";
    }
}
