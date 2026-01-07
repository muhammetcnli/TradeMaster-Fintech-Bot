package com.trademaster.fintech_core;

import com.trademaster.fintech_core.service.PriceService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TestRunner implements CommandLineRunner {

    private final PriceService priceService;

    public TestRunner(PriceService priceService) {
        this.priceService = priceService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- YENİ API TESTİ BAŞLIYOR ---");

        BigDecimal btcPrice = priceService.getLatestPrice("BTC");
        System.out.println("Bitcoin Fiyatı (CoinGecko): $" + btcPrice);


        BigDecimal zlotyPrice = priceService.getLatestPrice("EURPLN");
        System.out.println("1 Euro kaç Zloty (Frankfurter): " + zlotyPrice);

        System.out.println("--- TEST BİTTİ ---");
    }
}