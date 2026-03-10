package com.trademaster.fintech_core.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WatchRequest {

    UUID userId;
    String symbol;
}
