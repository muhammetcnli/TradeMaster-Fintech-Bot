package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UUID registerOrLogin(String provider, String externalId, String username) {
        Optional<User> userOptional = userRepository.findByAuthProviderAndExternalId(provider, externalId);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setAuthProvider(provider);
        newUser.setExternalId(externalId);
        newUser.setBalance(BigDecimal.valueOf(100000));
        userRepository.save(newUser);

        return newUser.getId();
    }
}
