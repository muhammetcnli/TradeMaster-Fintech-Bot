package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.AssetItemDto;
import com.trademaster.fintech_core.dto.MarketPriceDto;
import com.trademaster.fintech_core.dto.PortfolioDto;
import com.trademaster.fintech_core.entity.Asset;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.entity.UserAsset;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {

    private final MarketDataService marketDataService;
    private UserService userService;

    public PortfolioService(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
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
     * Finds user,
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
}
