package com.travelapi.patterns;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheManager {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private long hits = 0;
    private long misses = 0;

    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime timestamp;

        public CacheEntry(Object value) {
            this.value = value;
            this.timestamp = LocalDateTime.now();
        }

        public Object getValue() {
            return value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry entry = cache.get(key);

        if (entry != null) {
            hits++;
            return (T) entry.getValue();
        }

        misses++;
        return null;
    }

    public void put(String key, Object value) {
        cache.put(key, new CacheEntry(value));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public void evictByPattern(String pattern) {
        String prefix = pattern.replace("*", "");
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public void clear() {
        cache.clear();
        hits = 0;
        misses = 0;
    }

    public int size() {
        return cache.size();
    }

    public double getHitRatio() {
        long total = hits + misses;
        if (total == 0) return 0.0;
        return (double) hits / total * 100;
    }

    public CacheStats getStats() {
        return new CacheStats(cache.size(), hits, misses, getHitRatio());
    }

    public static class CacheStats {
        private final int size;
        private final long hits;
        private final long misses;
        private final double hitRatio;

        public CacheStats(int size, long hits, long misses, double hitRatio) {
            this.size = size;
            this.hits = hits;
            this.misses = misses;
            this.hitRatio = hitRatio;
        }

        public int getSize() {
            return size;
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        public double getHitRatio() {
            return hitRatio;
        }

        @Override
        public String toString() {
            return String.format("CacheStats[size=%d, hits=%d, misses=%d, hitRatio=%.2f%%]",
                    size, hits, misses, hitRatio);
        }
    }
}
