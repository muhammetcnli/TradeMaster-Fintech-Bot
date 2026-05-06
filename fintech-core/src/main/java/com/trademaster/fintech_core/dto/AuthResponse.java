package com.trademaster.fintech_core.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String username,
        String provider,
        String accessToken
) {
}

