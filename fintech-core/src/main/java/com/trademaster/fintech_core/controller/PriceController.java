package com.trademaster.fintech_core.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;


    @GetMapping("/check/{}")
    public ResponseEntity<BigDecimal> checkAsset(@PathVariable String symbol){
(
        // try to get price
        BigDecimal price = priceService.getLatestPrice(symbol);

        if(price == null || price.compareTo(BigDecimal.ZERO) == 0){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(price);

    }

}
