package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.dto.TradeRequest;
import com.trademaster.fintech_core.dto.WatchListDto;
import com.trademaster.fintech_core.dto.WatchRequest;
import com.trademaster.fintech_core.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDto> getUserPortfolio(@PathVariable UUID id){
            // get User portfolio from service
            PortfolioDto portfolioDto = portfolioService.getUserPortfolio(id);

            // return ok response
            return ResponseEntity.ok(portfolioDto);
    }

    @PostMapping("/watch/")
    public ResponseEntity<WatchListDto> watchAsset(@RequestBody WatchRequest request) throws IllegalAccessException {

        return ResponseEntity.ok(portfolioService.watchAsset(request.getUserId(), request.getSymbol()));

    }

    @PostMapping("/assets/{symbol}/buy")
    public ResponseEntity<PortfolioDto> buyAsset(@PathVariable String symbol,
                                                 @RequestBody TradeRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(portfolioService.buyAsset(request.getUserId(), symbol, request.getQuantity()));
    }

    @PostMapping("/assets/{symbol}/sell")
    public ResponseEntity<PortfolioDto> sellAsset(@PathVariable String symbol,
                                                  @RequestBody TradeRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(portfolioService.sellAsset(request.getUserId(), symbol, request.getQuantity()));
    }

}
