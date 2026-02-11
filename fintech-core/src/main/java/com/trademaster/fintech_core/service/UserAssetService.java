package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.entity.Asset;
import com.trademaster.fintech_core.domain.entity.User;
import com.trademaster.fintech_core.domain.entity.UserAsset;
import com.trademaster.fintech_core.repository.AssetRepository;
import com.trademaster.fintech_core.repository.UserAssetRepository;
import com.trademaster.fintech_core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAssetService {

    private final UserAssetRepository userAssetRepository;
    private final UserRepository userRepository;
    private final AssetRepository assetRepository;

    // get all assets of User
    public List<UserAsset> getUserAssets(UUID userId){
        return userRepository.findAllByUserId(userId);
    }


    // buy asset
    public void buyAsset(UUID userId, String symbol, @PathVariable BigDecimal quantity){

        // find user, if user not exists throw exception
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // find asset or build one
        Asset asset = assetRepository.findBySymbol(symbol.toUpperCase())
                .orElseGet(() -> assetRepository.save(Asset.builder()
                        .symbol(symbol)
                        .name(symbol.toUpperCase()) // symbol for now
                        .build()));

        

    }
}
