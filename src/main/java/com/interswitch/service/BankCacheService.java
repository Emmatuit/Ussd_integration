package com.interswitch.service;

import com.interswitch.client.BankApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankCacheService {

    private static final String BANK_LIST_KEY = "bank_list";
    private static final long CACHE_TTL_SECONDS = 86400; // 24 hours

    private final BankApiClient bankApiClient;
    private final RedisTemplate<String, List<Map<String, Object>>> bankListRedisTemplate;

    public List<Map<String, Object>> getBanks() {
        try {
            List<Map<String, Object>> cachedBanks = bankListRedisTemplate.opsForValue().get(BANK_LIST_KEY);

            if (cachedBanks != null && !cachedBanks.isEmpty()) {
                log.debug("Bank list loaded from Redis cache: {} banks", cachedBanks.size());
                return cachedBanks;
            }

            log.info("Bank list not in cache, fetching from staging API");
            String token = "a3b3b6f1-dbe0-40ce-830e-2cdcd4679099";
            List<Map<String, Object>> banks = bankApiClient.getCommercialBanks(token);

            if (banks != null && !banks.isEmpty()) {
                bankListRedisTemplate.opsForValue().set(BANK_LIST_KEY, banks, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
                log.info("Bank list cached in Redis: {} banks", banks.size());
            }

            return banks != null ? banks : List.of();
        } catch (Exception e) {
            log.error("Failed to get banks, falling back to API", e);
            String token = "a3b3b6f1-dbe0-40ce-830e-2cdcd4679099";
            List<Map<String, Object>> banks = bankApiClient.getCommercialBanks(token);
            return banks != null ? banks : List.of();
        }
    }

    public void refreshCache() {
        bankListRedisTemplate.delete(BANK_LIST_KEY);
        getBanks();
    }

    public void clearCache() {
        bankListRedisTemplate.delete(BANK_LIST_KEY);
    }
}