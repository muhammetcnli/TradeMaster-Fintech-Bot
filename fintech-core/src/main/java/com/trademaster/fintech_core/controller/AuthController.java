package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.service.AuthService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UUID login(@RequestBody AuthRequest request) {
        return authService.registerOrLogin(request.getProvider(), request.getExternalId(), request.getUsername());
    }

    @Data
    public static class AuthRequest {
        private String provider;
        private String externalId;
        private String username;
    }
}
