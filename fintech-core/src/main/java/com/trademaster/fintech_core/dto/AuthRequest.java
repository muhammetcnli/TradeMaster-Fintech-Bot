package com.trademaster.fintech_core.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String provider;
    private String externalId;
    private String username;
}

