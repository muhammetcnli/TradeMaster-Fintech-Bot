package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.domain.AssetType;
import com.trademaster.fintech_core.domain.entity.Asset;
import com.trademaster.fintech_core.repository.AssetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public Asset findOrCreateAsset(String symbol){
        String upperSymbol = symbol.toUpperCase();

        // check database for existing asset
        Optional<Asset> existingAsset = assetRepository.findBySymbol(symbol);

        // asset exists return the asset
        if (existingAsset.isPresent()){
            return existingAsset.get();
        }

        Asset newAsset = new Asset();
        newAsset.setSymbol(upperSymbol);
        newAsset.setName(upperSymbol);

        // handle type field
        if(upperSymbol.length() == 6 && !upperSymbol.contains("BTC") && !upperSymbol.contains("ETH")){
            newAsset.setType(AssetType.FOREX);
        } else if ( upperSymbol.length() == 3){
            newAsset.setType(AssetType.CRYPTO);
        } else {
            newAsset.setType(AssetType.STOCK);
        }

        newAsset.setIsActive(true);

        return newAsset;
    }
}
