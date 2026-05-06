package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AuthResponse;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.repository.UserRepository;
import com.trademaster.fintech_core.security.TokenHasher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final com.trademaster.fintech_core.security.TokenHasher tokenHasher;

    public AuthService(UserRepository userRepository, TokenHasher tokenHasher) {
        this.userRepository = userRepository;
        this.tokenHasher = tokenHasher;
    }

    public AuthResponse registerOrLogin(String provider, String externalId, String username) {
        User user = getOrCreateUser(provider, externalId, username);

        String rawToken = tokenHasher.generateRawToken();
        user.setSessionTokenHash(tokenHasher.hash(rawToken));
        user.setSessionTokenIssuedAt(Instant.now());
        userRepository.save(user);

        return new AuthResponse(user.getId(), user.getUsername(), user.getAuthProvider(), rawToken);
    }

    public UUID resolveTelegramUserId(String externalId, String username) {
        return getOrCreateUser("TELEGRAM", externalId, username).getId();
    }

    public Optional<User> findByAccessToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return Optional.empty();
        }

        String tokenHash = tokenHasher.hash(rawToken);
        return userRepository.findBySessionTokenHash(tokenHash);
    }

    private User getOrCreateUser(String provider, String externalId, String username) {
        validateIdentity(provider, externalId);

        Optional<User> existingUser = userRepository.findByAuthProviderAndExternalId(provider.trim().toUpperCase(), externalId.trim());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            boolean changed = false;
            if (username != null && !username.isBlank()) {
                user.setUsername(username.trim());
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
            return user;
        }

        User newUser = new User();
        newUser.setUsername(resolveUsername(username, provider, externalId));
        newUser.setAuthProvider(provider.trim().toUpperCase());
        newUser.setExternalId(externalId.trim());
        newUser.setBalance(BigDecimal.valueOf(100000));

        userRepository.save(newUser);
        return newUser;
    }

    private void validateIdentity(String provider, String externalId) {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider cannot be blank");
        }
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("External ID cannot be blank");
        }
    }

    private String resolveUsername(String username, String provider, String externalId) {
        if (username != null && !username.isBlank()) {
            return username.trim();
        }

        return provider.trim().toLowerCase() + "_" + externalId.trim();
    }
}
