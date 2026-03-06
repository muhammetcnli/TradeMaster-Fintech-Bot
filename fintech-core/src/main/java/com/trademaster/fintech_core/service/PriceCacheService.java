package com.trademaster.fintech_core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trademaster.fintech_core.domain.dto.PriceQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PriceCacheService {

    private static final String PRICE_KEY_PREFIX = "price:";
    private static final String COOLDOWN_KEY_PREFIX = "price:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<String, CacheEntry> localCache = new ConcurrentHashMap<>();
    private final Map<String, Long> localCooldown = new ConcurrentHashMap<>();

    @Value("${price.cache.ttl-seconds:60}")
    private long priceTtlSeconds;

    @Value("${price.cache.cooldown-seconds:30}")
    private long cooldownTtlSeconds;

    /**
     * Returns the cached price for a symbol.
     * It returns empty when the cache is missing or invalid.
     */
    public Optional<PriceQuote> getPrice(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return Optional.empty();
        }

        String normalized = symbol.toUpperCase();
        String key = PRICE_KEY_PREFIX + normalized;
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value != null && !value.isBlank()) {
                return Optional.of(objectMapper.readValue(value, PriceQuote.class));
            }
        } catch (Exception ex) {
            try {
                stringRedisTemplate.delete(key);
            } catch (Exception ignored) {
            }
        }

        CacheEntry entry = localCache.get(normalized);
        if (entry != null && entry.expiresAt > System.currentTimeMillis()) {
            return Optional.of(entry.quote);
        }
        return Optional.empty();
    }

    /**
     * Stores the latest price in cache with a short TTL.
     */
    public void putPrice(PriceQuote quote) {
        if (quote == null || quote.symbol() == null || quote.symbol().isBlank()) {
            return;
        }

        String normalized = quote.symbol().toUpperCase();
        String key = PRICE_KEY_PREFIX + normalized;
        long expiresAt = System.currentTimeMillis() + (priceTtlSeconds * 1000);
        try {
            String value = objectMapper.writeValueAsString(quote);
            stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(priceTtlSeconds));
        } catch (Exception ignored) {
        }

        localCache.put(normalized, new CacheEntry(quote, expiresAt));
    }

    /**
     * Returns true when a symbol is on cooldown.
     * This prevents rapid upstream retries after failures.
     */
    public boolean isOnCooldown(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return false;
        }

        String normalized = symbol.toUpperCase();
        String key = COOLDOWN_KEY_PREFIX + normalized;
        try {
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
                return true;
            }
        } catch (Exception ignored) {
        }

        Long localExpiry = localCooldown.get(normalized);
        return localExpiry != null && localExpiry > System.currentTimeMillis();
    }

    /**
     * Sets a short cooldown for a symbol after an upstream failure.
     */
    public void setCooldown(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return;
        }

        String normalized = symbol.toUpperCase();
        String key = COOLDOWN_KEY_PREFIX + normalized;
        long expiresAt = System.currentTimeMillis() + (cooldownTtlSeconds * 1000);
        try {
            stringRedisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(cooldownTtlSeconds));
        } catch (Exception ignored) {
        }

        localCooldown.put(normalized, expiresAt);
    }

    private record CacheEntry(PriceQuote quote, long expiresAt) {
    }
}
