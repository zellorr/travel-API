package com.travelapi.controller;

import com.travelapi.patterns.CacheManager;
import com.travelapi.patterns.LoggingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for cache management.
 * Provides endpoints to monitor and manage the cache.
 */
@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheManager cacheManager;
    private final LoggingService loggingService;

    public CacheController(CacheManager cacheManager, LoggingService loggingService) {
        this.cacheManager = cacheManager;
        this.loggingService = loggingService;
    }

    /**
     * Get cache statistics
     * GET /api/cache/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<CacheManager.CacheStats> getCacheStats() {
        loggingService.info("Getting cache statistics");
        CacheManager.CacheStats stats = cacheManager.getStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Clear entire cache
     * DELETE /api/cache
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearCache() {
        loggingService.info("Clearing entire cache");
        cacheManager.clear();
        return ResponseEntity.ok(Map.of(
                "message", "Cache cleared successfully",
                "size", "0"
        ));
    }

    /**
     * Evict specific cache key
     * DELETE /api/cache/{key}
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<Map<String, String>> evictCacheKey(@PathVariable String key) {
        loggingService.info("Evicting cache key: " + key);
        cacheManager.evict(key);
        return ResponseEntity.ok(Map.of(
                "message", "Cache key evicted successfully",
                "key", key
        ));
    }

    /**
     * Evict cache keys by pattern
     * DELETE /api/cache/pattern/{pattern}
     */
    @DeleteMapping("/pattern/{pattern}")
    public ResponseEntity<Map<String, String>> evictCacheByPattern(@PathVariable String pattern) {
        loggingService.info("Evicting cache by pattern: " + pattern);
        cacheManager.evictByPattern(pattern);
        return ResponseEntity.ok(Map.of(
                "message", "Cache keys matching pattern evicted successfully",
                "pattern", pattern
        ));
    }

    /**
     * Get cache size
     * GET /api/cache/size
     */
    @GetMapping("/size")
    public ResponseEntity<Map<String, Integer>> getCacheSize() {
        int size = cacheManager.size();
        return ResponseEntity.ok(Map.of("size", size));
    }
}