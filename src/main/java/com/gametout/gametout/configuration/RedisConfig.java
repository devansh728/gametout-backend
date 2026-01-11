package com.gametout.gametout.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        GenericJackson2JsonRedisSerializer serializer = genericSerializer();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(genericSerializer()))
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    private GenericJackson2JsonRedisSerializer genericSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.EVERYTHING, 
                JsonTypeInfo.As.PROPERTY
        );
        
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}





















// package com.gametout.gametout.configuration;

// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.cache.RedisCacheConfiguration;
// import org.springframework.data.redis.cache.RedisCacheManager;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.RedisSerializationContext;
// import com.fasterxml.jackson.databind.json.JsonMapper;
// import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// import java.time.Duration;

// @Configuration
// @EnableCaching
// public class RedisConfig {

//     @Bean
//     public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

//         ObjectMapper objectMapper = JsonMapper.builder()
//                 .addModule(new JavaTimeModule())
//                 .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                 // .activateDefaultTyping(
//                 // LaissezFaireSubTypeValidator.instance,
//                 // ObjectMapper.DefaultTyping.NON_FINAL)
//                 .build();

//         // RedisSerializationContext.SerializationPair<Object> valueSerializer =
//         // RedisSerializationContext.SerializationPair
//         // .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

//         // RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//         // .serializeValuesWith(valueSerializer)
//         // .entryTtl(Duration.ofMinutes(10));
//         Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

//         RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                 .serializeValuesWith(
//                         RedisSerializationContext.SerializationPair.fromSerializer(serializer))
//                 .entryTtl(Duration.ofMinutes(10));

//         return RedisCacheManager.builder(factory)
//                 .cacheDefaults(config)
//                 .build();
//     }
// }
