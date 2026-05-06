package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.dto.TradeRequest;
import com.trademaster.fintech_core.dto.WatchListDto;
import com.trademaster.fintech_core.dto.WatchRequest;
import com.trademaster.fintech_core.service.PortfolioService;
import com.trademaster.fintech_core.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final CurrentUserService currentUserService;

    @GetMapping("/me")
    public ResponseEntity<PortfolioDto> getUserPortfolio() {
            UUID userId = currentUserService.requireCurrentUserId();
            PortfolioDto portfolioDto = portfolioService.getUserPortfolio(userId);
            return ResponseEntity.ok(portfolioDto);
    }

    @PostMapping("/watch")
    public ResponseEntity<WatchListDto> watchAsset(@RequestBody WatchRequest request) throws IllegalAccessException {
        UUID userId = currentUserService.requireCurrentUserId();
        return ResponseEntity.ok(portfolioService.watchAsset(userId, request.getSymbol()));

    }

    @PostMapping("/assets/{symbol}/buy")
    public ResponseEntity<PortfolioDto> buyAsset(@PathVariable String symbol,
                                                 @RequestBody TradeRequest request) throws IllegalAccessException {
        UUID userId = currentUserService.requireCurrentUserId();
        return ResponseEntity.ok(portfolioService.buyAsset(userId, symbol, request.getQuantity()));
    }

    @PostMapping("/assets/{symbol}/sell")
    public ResponseEntity<PortfolioDto> sellAsset(@PathVariable String symbol,
                                                  @RequestBody TradeRequest request) throws IllegalAccessException {
        UUID userId = currentUserService.requireCurrentUserId();
        return ResponseEntity.ok(portfolioService.sellAsset(userId, symbol, request.getQuantity()));
    }

}
