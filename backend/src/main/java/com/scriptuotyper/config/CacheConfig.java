package com.scriptuotyper.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json())
                );

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                "bible:books", defaultConfig.entryTtl(Duration.ofHours(24)),
                "bible:chapter", defaultConfig.entryTtl(Duration.ofHours(24)),
                "ranking:top", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "ranking:sarangbang", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "ranking:monthly", defaultConfig.entryTtl(Duration.ofMinutes(30)),
                "board:list", defaultConfig.entryTtl(Duration.ofMinutes(5)),
                "board:detail", defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
