package com.trademaster.fintech_core.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;


public interface PriceService {

    BigDecimal getLatestPrice(String symbol);
}
