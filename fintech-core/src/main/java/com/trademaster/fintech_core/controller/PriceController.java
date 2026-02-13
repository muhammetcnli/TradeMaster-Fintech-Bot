package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.domain.dto.ErrorResponse;
import com.trademaster.fintech_core.domain.dto.PriceQuote;
import com.trademaster.fintech_core.domain.dto.PriceResponse;
import com.trademaster.fintech_core.service.PriceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9-]{1,20}$");

    private final PriceService priceService;

    /**
     * Returns the latest price for a symbol.
     * It validates the symbol format and returns a standard response.
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<?> checkAsset(@PathVariable String symbol, HttpServletRequest request){
        if (symbol == null || symbol.isBlank()) {
            return badRequest("Symbol is required.", request.getRequestURI());
        }

        String normalized = symbol.trim().toUpperCase();
        if (!SYMBOL_PATTERN.matcher(normalized).matches()) {
            return badRequest("Symbol format is invalid.", request.getRequestURI());
        }

        Optional<PriceQuote> quote = priceService.getLatestPrice(normalized);
        if (quote.isEmpty()) {
            return notFound("Symbol not found.", request.getRequestURI());
        }

        return ResponseEntity.ok(new PriceResponse("OK", quote.get()));
    }

    private ResponseEntity<ErrorResponse> badRequest(String message, String path) {
        ErrorResponse body = new ErrorResponse("ERROR", "BAD_REQUEST", message, Instant.now().toString(), path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<ErrorResponse> notFound(String message, String path) {
        ErrorResponse body = new ErrorResponse("ERROR", "NOT_FOUND", message, Instant.now().toString(), path);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
