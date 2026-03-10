package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.dto.WatchListDto;
import com.trademaster.fintech_core.dto.WatchRequest;
import com.trademaster.fintech_core.entity.UserAsset;
import com.trademaster.fintech_core.service.PortfolioService;
import lombok.NoArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@NoArgsConstructor
@RequestMapping("api/v1/portfolio")
public class PortfolioController {

    private PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<PortfolioDto> getUserPortfolio(@PathVariable UUID id){
            // get User portfolio from service
            PortfolioDto portfolioDto = portfolioService.getUserPortfolio(id);

            // return ok response
            return ResponseEntity.ok(portfolioDto);
    }

    @PostMapping("/watch/")
    public ResponseEntity<WatchListDto> watchAsset(@RequestBody WatchRequest request){

        return ResponseEntity.ok(portfolioService.watchAsset(request.getUserId(), request.getSymbol()));

    }

}
