package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class TelegramBotService {

    private PriceService priceService;
    private final UserRepository userRepository;

    public TelegramBotService(UserRepository userRepository, PriceService priceService) {
        this.userRepository = userRepository;
        this.priceService = priceService;
    }


}
