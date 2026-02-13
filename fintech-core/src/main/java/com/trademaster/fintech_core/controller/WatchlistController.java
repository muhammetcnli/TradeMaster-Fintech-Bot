package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.domain.entity.Asset;
import com.trademaster.fintech_core.service.UserAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class WatchlistController {

    private final UserAssetService userAssetService;

    /**
     * Adds a symbol to the user watchlist.
     * It enables alerts by default.
     */
    @GetMapping("/add")
    public ResponseEntity<?> addToWatchlist(@RequestParam UUID userId, @RequestParam String symbol){
        userAssetService.addAssetToWatchlist(userId, symbol);

        return ResponseEntity.ok("Added to the watchlist.");
    }

    /**
     * Buys a quantity of an asset for the user.
     * It uses a simple response body for now.
     */
    @GetMapping("/buy/{quantity}")
    public ResponseEntity<?> buyAsset(@RequestParam UUID userId, @RequestParam String symbol, @PathVariable BigDecimal quantity){
        userAssetService.buyAsset(userId, symbol, quantity);

        return ResponseEntity.ok("Bought.");
    }

    /**
     * Sells a quantity of an asset for the user.
     * It uses a simple response body for now.
     */
    @GetMapping("/sell/{quantity}")
    public ResponseEntity<?> sellAsset(@RequestParam UUID userId, @RequestParam String symbol, @PathVariable BigDecimal quantity){

        userAssetService.sellAsset(userId, symbol, quantity);

        return ResponseEntity.ok("Sold.");
    }

    /**
     * Returns the assets for the user portfolio.
     * This list does not include quantities yet.
     */
    @GetMapping("/portfolio")
    public List<Asset> getPortfolio(@RequestParam UUID userId){

        return userAssetService.getUserAssets(userId);
    }
}
