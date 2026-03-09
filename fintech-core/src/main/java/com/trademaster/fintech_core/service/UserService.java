package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@NoArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public User getUserById(UUID uuid){
        return userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }


}
