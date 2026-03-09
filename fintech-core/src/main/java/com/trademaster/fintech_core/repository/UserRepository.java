package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAuthProviderAndExternalId(String provider, String externalId);
}
