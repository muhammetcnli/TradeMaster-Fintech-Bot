package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.UserAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAssetRepository extends JpaRepository<UserAsset, UUID> {
    List<UserAsset> findAllByUserId(UUID userId);

    Optional<UserAsset> findByUserIdAndAsset_Symbol(UUID userId, String symbol);
}
