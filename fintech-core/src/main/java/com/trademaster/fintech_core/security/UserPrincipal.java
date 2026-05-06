package com.trademaster.fintech_core.security;

import java.util.UUID;

public record UserPrincipal(UUID userId, String username, String provider) {
}

