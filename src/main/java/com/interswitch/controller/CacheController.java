package com.interswitch.controller;


import com.interswitch.service.BankCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final BankCacheService bankCacheService;

    @PostMapping("/clear-banks")
    public Map<String, String> clearBanks() {
        bankCacheService.clearCache();
        return Map.of("status", "cleared");
    }

    @PostMapping("/refresh-banks")
    public Map<String, Object> refreshBanks() {
        bankCacheService.refreshCache();
        return Map.of("status", "refreshed");
    }
}
