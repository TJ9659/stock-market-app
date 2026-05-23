package com.marketsense.config;


import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.registerCustomCache("stocks", caffeineConfig(1, TimeUnit.MINUTES));
        cacheManager.registerCustomCache("market_insights", caffeineConfig(30, TimeUnit.MINUTES));

        return cacheManager;
    }

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineConfig(long duration, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(500)
                .build();
    }
}
