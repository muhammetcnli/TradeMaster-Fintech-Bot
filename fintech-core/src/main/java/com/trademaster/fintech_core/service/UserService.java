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

    /**
     * Retrieve user by UUID
     */
    public User getUserById(UUID uuid){
        return userRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("User not found!"));
    }

    /**
     * Find or create a Telegram user
     * @param telegramUserId Telegram user ID (from Telegram API)
     * @param telegramUsername Telegram username (nullable)
     * @return User entity linked to Telegram
     */
    public User findOrCreateTelegramUser(Long telegramUserId, String telegramUsername) {
        String externalId = String.valueOf(telegramUserId);
        String username = telegramUsername != null && !telegramUsername.isBlank()
                ? telegramUsername
                : "telegram_" + telegramUserId;

        return userRepository.findByAuthProviderAndExternalId("TELEGRAM", externalId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .authProvider("TELEGRAM")
                            .externalId(externalId)
                            .balance(java.math.BigDecimal.valueOf(100000))
                            .build();
                    return userRepository.save(newUser);
                });
    }

    /**
     * Get user by Telegram user ID
     */
    public User getUserByTelegramId(Long telegramUserId) {
        String externalId = String.valueOf(telegramUserId);
        return userRepository.findByAuthProviderAndExternalId("TELEGRAM", externalId)
                .orElseThrow(() -> new RuntimeException("Telegram user not found: " + telegramUserId));
    }
}
