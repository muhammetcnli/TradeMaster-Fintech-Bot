package com.trademaster.fintech_core.repository;

import com.trademaster.fintech_core.domain.entity.UserAsset;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssetRepository {
    // get all the users follow list
    List<UserAsset> findByUserId(Long userId);

    //
    Optional<UserAsset> findByUserIdAndAssetId(Long userId, Long assetId);
}
