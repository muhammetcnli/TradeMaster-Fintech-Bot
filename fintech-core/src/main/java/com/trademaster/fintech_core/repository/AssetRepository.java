package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    Optional<Asset> findBySymbol(String symbol);
}
