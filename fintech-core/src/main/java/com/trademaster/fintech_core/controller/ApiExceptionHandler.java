package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.domain.dto.ErrorResponse;
import com.trademaster.fintech_core.service.PriceProviderException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Returns a consistent error body for upstream provider failures.
     * This is used when an external API is unreachable or invalid.
     */
    @ExceptionHandler(PriceProviderException.class)
    public ResponseEntity<ErrorResponse> handlePriceProvider(PriceProviderException ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                "ERROR",
                "UPSTREAM_FAILURE",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }
}
