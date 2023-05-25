package com.yunli.guanxian.data.exchange.guanxiandataexchange.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

/**
 * Redis配置  开启spring cache缓存
 *
 */
//@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * spring cache redis缓存有效时长
     */
    @Value("${spring.cache.redis.time-to-live:500s}")
    private Duration timeToLive;

    /**
     * RedisTemplate配置 在单独使用redisTemplate的时候 重新定义序列化方式
     */
    @Bean
    @Order
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> jackson2JsonRedisSerializer = valueSerializer();

        // 配置redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());// key序列化
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);// value序列化
        redisTemplate.setHashKeySerializer(RedisSerializer.string());// Hash key序列化
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);// Hash value序列化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    private RedisSerializer<Object> valueSerializer() {
        //GenericJackson2JsonRedisSerializer 和 Jackson2JsonRedisSerializer 两者都能系列化成json，但是前者会在json中加入@class属性，类的全路径包名，方便反系列化。
        return new GenericJackson2JsonRedisSerializer();

//        // 设置序列化
//        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
//                Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//        return jackson2JsonRedisSerializer;
    }

    @Primary
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        //缓存配置对象
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        //设置序列化的域(属性,方法etc)以及修饰范围,Any包括private,public 默认是public的
        //ALL所有方位,ANY所有修饰符
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //enableDefaultTyping 原来的方法存在漏洞,2.0后改用如下配置
        //指定输入的类型
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);
        //如果java.time包下Json报错,添加如下两行代码
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.registerModule(new JavaTimeModule());
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisCacheConfiguration = redisCacheConfiguration.entryTtl(timeToLive)
//                .disableCachingNullValues() ;            //如果是空值，不缓存
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))         //设置key序列化器
                //设置value序列化器
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));

        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration).build();
    }
}
