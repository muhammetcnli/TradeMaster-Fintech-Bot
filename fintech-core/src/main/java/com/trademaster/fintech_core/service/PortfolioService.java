package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.*;
import com.trademaster.fintech_core.entity.Asset;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.entity.UserAsset;
import com.trademaster.fintech_core.repository.AssetRepository;
import com.trademaster.fintech_core.repository.UserAssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {

    private final MarketDataService marketDataService;
    private final UserService userService;
    private final AssetRepository assetRepository;
    private final UserAssetRepository userAssetRepository;

    public PortfolioService(MarketDataService marketDataService, UserService userService, AssetRepository assetRepository, UserAssetRepository userAssetRepository) {
        this.marketDataService = marketDataService;
        this.userService = userService;
        this.assetRepository = assetRepository;
        this.userAssetRepository = userAssetRepository;
    }

    private BigDecimal calculatePnL(BigDecimal averageCost, BigDecimal currentPrice){
        if (averageCost.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return currentPrice.subtract(averageCost)
                .divide(averageCost)
                .multiply(new BigDecimal(100));
    }

    public BigDecimal calcTotalPortfolioValue(BigDecimal balance,List<AssetItemDto> assets){

       List<BigDecimal> values = assets.stream().map(
               assetItem -> assetItem.getQuantity().multiply(assetItem.getCurrentPrice())
        ).toList();

       BigDecimal totalAssets = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

       return totalAssets.add(balance);

    }

    /**
     *
     * @param userId
     * Finds user, get asset's
     * @return PortfolioDto
     */
    public PortfolioDto getUserPortfolio(UUID userId){

        // get user
        User user = userService.getUserById(userId);

        // get userAssets from user as a stream and map them
        List<AssetItemDto> assets = user.getAssets().stream().map(
                ua -> {
                    // get priceDto for each userAsset
                    MarketPriceDto priceDto = marketDataService.getCurrentPrice(ua.getAsset().getSymbol());

                    // convert priceDto to price
                    BigDecimal currentPrice = priceDto.getPrice();

                    // get profitLossPercentage
                    BigDecimal pnl = calculatePnL(ua.getAverageCost(), currentPrice);

                    // return reach AssetItemDto with currentPrice, pnl, and same values from UserAsset
                    return AssetItemDto.builder()
                            .symbol(ua.getAsset().getSymbol())
                            .currentPrice(currentPrice)
                            .quantity(ua.getQuantity())
                            .averageCost(ua.getAverageCost())
                            .profitLossPercentage(pnl)
                            .build();
                }).toList();

        return PortfolioDto.builder()
                .assets(assets)
                .currentBalance(user.getBalance())
                .username(user.getUsername())
                .totalPortfolioValue(calcTotalPortfolioValue(user.getBalance(),assets))
                .userId(userId)
                .build();
    }

    // Make sure these:
    // Checks if the asset exists, if not gets one with symbol
    // check's if user watches this asset or not
    // if not, watches the asset
    //
    public WatchListDto watchAsset(UUID userId, String symbol){

        // check if symbol null
        if (symbol == null || symbol.isBlank()){
            throw new IllegalAccessException("Symbol cannot be blank");
        }

        // normalize symbol
        String normalized = symbol.trim().toUpperCase();

        // get user
        User user = userService.getUserById(userId);

        // Get asset of create new one
        Asset asset = assetRepository.findBySymbol(normalized)
                .orElseGet(() -> assetRepository.save(
                        Asset.builder()
                                .symbol(normalized)
                                .name(normalized)
                                .assetType(AssetType.CRYPTO)
                                .build()
                ));

        // get existing watchItem or return null
        UserAsset existing = userAssetRepository.findByUserIdAndAsset_Symbol(userId, normalized)
                .orElse(null);

        // if watchItem not exists, create one and save to UserAssetRepository instance
        if (existing == null){
            UserAsset watchItem = UserAsset.builder()
                    .asset(asset)
                    .user(user)
                    .quantity(BigDecimal.ZERO)
                    .averageCost(BigDecimal.ZERO)
                    .build();

            userAssetRepository.save(watchItem);
        }

        BigDecimal currentPrice = marketDataService.getCurrentPrice(normalized).getPrice();

        return WatchListDto.builder()
                .symbol(normalized)
                .currentPrice(currentPrice)
                .targetPrice(null)
                .isAlertEnabled(false)
                .trend("UNKNOWN")
                .build();

    }
}
