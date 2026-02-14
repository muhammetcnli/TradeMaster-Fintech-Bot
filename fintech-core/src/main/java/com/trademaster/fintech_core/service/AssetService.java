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

    /**
     * Finds an asset by symbol or creates and saves it.
     * It also sets a basic asset type based on the symbol format.
     */
    @Transactional
    public Asset findOrCreateAsset(String symbol){
        String upperSymbol = symbol.toUpperCase();

        Optional<Asset> existingAsset = assetRepository.findBySymbol(upperSymbol);
        if (existingAsset.isPresent()){
            return existingAsset.get();
        }

        Asset newAsset = new Asset();
        newAsset.setSymbol(upperSymbol);
        newAsset.setName(upperSymbol);

        if (upperSymbol.length() == 6 && !upperSymbol.contains("BTC") && !upperSymbol.contains("ETH")){
            newAsset.setType(AssetType.FOREX);
        } else if (upperSymbol.length() == 3){
            newAsset.setType(AssetType.CRYPTO);
        } else {
            newAsset.setType(AssetType.STOCK);
        }

        newAsset.setIsActive(true);

        return assetRepository.save(newAsset);
    }
}
