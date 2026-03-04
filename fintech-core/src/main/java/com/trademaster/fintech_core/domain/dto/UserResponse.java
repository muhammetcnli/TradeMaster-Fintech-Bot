package com.trademaster.fintech_core.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        Long chatId,
        String username,
        String firstName,
        String lastName,
        BigDecimal usdBalance,
        LocalDateTime registeredAt,
        Boolean isActive
) {
}
