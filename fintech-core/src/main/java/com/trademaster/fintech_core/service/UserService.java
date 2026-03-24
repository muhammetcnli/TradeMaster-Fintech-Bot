package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(UUID uuid){
        return userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }


}
