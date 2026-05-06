package com.trademaster.fintech_core.controller;

import com.trademaster.fintech_core.dto.AuthResponse;
import com.trademaster.fintech_core.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.registerOrLogin(request.provider, request.externalId, request.username);
    }

    public static class AuthRequest {
        public String provider;
        public String externalId;
        public String username;
    }
}
