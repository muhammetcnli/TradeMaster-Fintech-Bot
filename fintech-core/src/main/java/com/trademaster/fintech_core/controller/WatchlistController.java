package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.domain.entity.Asset;
import com.trademaster.fintech_core.service.UserAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class WatchlistController {

    private final UserAssetService userAssetService;



    // add asset to the watchlist method, alert on certain values or percentage changes

    @GetMapping("/add")
    public ResponseEntity<?> addToWatchlist(UUID userId,String symbol){
        // alert enabled, quantity 0,
        userAssetService.addAssetToWatchlist(userId, symbol);

        return ResponseEntity.ok("Added to the watchlist.");
    }

    @GetMapping("/buy/{quantity}")
    public ResponseEntity<?> buyAsset(UUID userId, String symbol, @PathVariable BigDecimal quantity){
         userAssetService.buyAsset(userId, symbol, quantity);

         // TODO: implement DTO's
        return ResponseEntity.ok("Bought.");
    }

    @GetMapping("/sell/{quantity}")
    public ResponseEntity<?> sellAsset(String userId, String symbol, @PathVariable BigDecimal quantity){

        userAssetService.sellAsset(userId, symbol, quantity);

        return ResponseEntity.ok("Sold.");
    }

    @GetMapping("/portfolio")
    public List<Asset> getPortfolio(UUID userId){

        return userAssetService.getUserAssests(userId);
    }
    // see my assets method


    public
    // update asset method, change alert value or percentage

    //
}
