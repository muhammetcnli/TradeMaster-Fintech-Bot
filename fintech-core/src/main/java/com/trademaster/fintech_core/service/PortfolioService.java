package com.trademaster.fintech_core.service;

import com.trademaster.fintech_core.dto.*;
import com.trademaster.fintech_core.entity.Asset;
import com.trademaster.fintech_core.entity.Transaction;
import com.trademaster.fintech_core.entity.TransactionType;
import com.trademaster.fintech_core.entity.User;
import com.trademaster.fintech_core.entity.UserAsset;
import com.trademaster.fintech_core.repository.AssetRepository;
import com.trademaster.fintech_core.repository.TransactionRepository;
import com.trademaster.fintech_core.repository.UserAssetRepository;
import com.trademaster.fintech_core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PortfolioService {

    private final MarketDataService marketDataService;
    private final UserService userService;
    private final AssetRepository assetRepository;
    private final UserAssetRepository userAssetRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public PortfolioService(MarketDataService marketDataService, UserService userService, AssetRepository assetRepository, UserAssetRepository userAssetRepository, UserRepository userRepository, TransactionRepository transactionRepository) {
        this.marketDataService = marketDataService;
        this.userService = userService;
        this.assetRepository = assetRepository;
        this.userAssetRepository = userAssetRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    private BigDecimal calculatePnL(BigDecimal averageCost, BigDecimal currentPrice){
        if (averageCost.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return currentPrice.subtract(averageCost)
                .divide(averageCost, 8, RoundingMode.HALF_UP)
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

        // Read holdings directly from repository to avoid stale entity state in the same transaction.
        List<AssetItemDto> assets = userAssetRepository.findAllByUserId(userId).stream()
                .filter(ua -> ua.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .map(
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

    public List<WatchListDto> getWatchList(UUID userId) {
        return userAssetRepository.findAllByUserId(userId).stream()
                .filter(ua -> ua.getQuantity().compareTo(BigDecimal.ZERO) == 0)
                .map(ua -> {
                    String symbol = ua.getAsset().getSymbol();
                    BigDecimal currentPrice = marketDataService.getCurrentPrice(symbol).getPrice();

                    return WatchListDto.builder()
                            .symbol(symbol)
                            .currentPrice(currentPrice)
                            .targetPrice(null)
                            .isAlertEnabled(false)
                            .trend("UNKNOWN")
                            .build();
                })
                .toList();
    }

    // Make sure these:
    // Checks if the asset exists, if not gets one with symbol
    // check's if user watches this asset or not
    // if not, watches the asset
    //
    @Transactional
    public WatchListDto watchAsset(UUID userId, String symbol) throws IllegalAccessException {

        String normalized = normalizeSymbol(symbol);

        // get user
        User user = userService.getUserById(userId);

        // Get asset of create new one
        Asset asset = findOrCreateAsset(normalized);

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

        logTransaction(user, asset, TransactionType.WATCH, BigDecimal.ZERO, currentPrice);

        return WatchListDto.builder()
                .symbol(normalized)
                .currentPrice(currentPrice)
                .targetPrice(null)
                .isAlertEnabled(false)
                .trend("UNKNOWN")
                .build();

    }

    @Transactional
    public PortfolioDto buyAsset(UUID userId, String symbol, BigDecimal quantity) throws IllegalAccessException {
        String normalized = normalizeSymbol(symbol);
        validateQuantity(quantity);

        User user = userService.getUserById(userId);
        Asset asset = findOrCreateAsset(normalized);
        BigDecimal currentPrice = marketDataService.getCurrentPrice(normalized).getPrice();
        BigDecimal totalCost = currentPrice.multiply(quantity);

        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        UserAsset userAsset = userAssetRepository.findByUserIdAndAsset_Symbol(userId, normalized)
                .orElseGet(() -> UserAsset.builder()
                        .user(user)
                        .asset(asset)
                        .quantity(BigDecimal.ZERO)
                        .averageCost(BigDecimal.ZERO)
                        .build());

        BigDecimal previousQty = userAsset.getQuantity();
        BigDecimal newQty = previousQty.add(quantity);

        BigDecimal newAverageCost;
        if (previousQty.compareTo(BigDecimal.ZERO) == 0) {
            newAverageCost = currentPrice;
        } else {
            BigDecimal previousTotal = previousQty.multiply(userAsset.getAverageCost());
            BigDecimal newTotal = quantity.multiply(currentPrice);
            newAverageCost = previousTotal.add(newTotal)
                    .divide(newQty, 8, RoundingMode.HALF_UP);
        }

        userAsset.setQuantity(newQty);
        userAsset.setAverageCost(newAverageCost);
        userAssetRepository.save(userAsset);

        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        logTransaction(user, asset, TransactionType.BUY, quantity, currentPrice);

        return getUserPortfolio(userId);
    }

    @Transactional
    public PortfolioDto sellAsset(UUID userId, String symbol, BigDecimal quantity) throws IllegalAccessException {
        String normalized = normalizeSymbol(symbol);
        validateQuantity(quantity);

        User user = userService.getUserById(userId);
        UserAsset userAsset = userAssetRepository.findByUserIdAndAsset_Symbol(userId, normalized)
                .orElseThrow(() -> new IllegalStateException("Asset not found in portfolio"));

        if (userAsset.getQuantity().compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient asset quantity");
        }

        BigDecimal currentPrice = marketDataService.getCurrentPrice(normalized).getPrice();
        BigDecimal proceeds = currentPrice.multiply(quantity);
        BigDecimal remainingQty = userAsset.getQuantity().subtract(quantity);

        if (remainingQty.compareTo(BigDecimal.ZERO) == 0) {
            userAssetRepository.delete(userAsset);
            userAssetRepository.flush();
        } else {
            userAsset.setQuantity(remainingQty);
            userAssetRepository.save(userAsset);
        }

        user.setBalance(user.getBalance().add(proceeds));
        userRepository.save(user);

        logTransaction(user, userAsset.getAsset(), TransactionType.SELL, quantity, currentPrice);

        return getUserPortfolio(userId);
    }

    private void logTransaction(User user, Asset asset, TransactionType type, BigDecimal quantity, BigDecimal price) {
        transactionRepository.save(Transaction.builder()
                .user(user)
                .asset(asset)
                .type(type)
                .quantity(quantity)
                .price(price)
                .timestamp(Instant.now())
                .build());
    }

    private String normalizeSymbol(String symbol) throws IllegalAccessException {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalAccessException("Symbol cannot be blank");
        }
        return symbol.trim().toUpperCase();
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private Asset findOrCreateAsset(String normalizedSymbol) {
        return assetRepository.findBySymbol(normalizedSymbol)
                .orElseGet(() -> assetRepository.save(
                        Asset.builder()
                                .symbol(normalizedSymbol)
                                .name(normalizedSymbol)
                                .assetType(marketDataService.detectAssetType(normalizedSymbol))
                                .build()
                ));
    }
}
