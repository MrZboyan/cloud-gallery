package com.common.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisAutoConfiguration {

    /**
     * 配置RedisTemplate
     * 使用String序列化Key，JSON序列化Value，支持Java 8时间类型
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("初始化 RedisTemplate，使用 JSON 序列化");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        // Key 使用 String 序列化
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // Value 使用 JSON 序列化，支持 Java 8 日期时间
        ObjectMapper objectMapper = this.createObjectMapper();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        // 应用配置
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置RedissonClient（用于分布式锁等高级功能）
     * 只有在引入了Redisson依赖且未自定义RedissonClient时才会创建
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @ConditionalOnProperty(prefix = "redisson", name = "enabled", matchIfMissing = true)
    public RedissonClient redissonClient(RedissonProperties properties) {
        log.info("初始化 RedissonClient, 地址: {}:{}", properties.getHost(), properties.getPort());
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setDatabase(properties.getDatabase())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(20)           // 连接池大小
                .setConnectionMinimumIdleSize(10)    // 最小空闲连接
                .setDnsMonitoringInterval(10000L);   // DNS监控间隔
        return Redisson.create(config);
    }


    /**
     * 创建 ObjectMapper 支持Java 8时间类型
     * 使用 ISO-8601 格式
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 支持 LocalDateTime 等 Java 8 时间类型
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用时间戳格式，使用 ISO-8601 字符串格式
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性（反序列化时）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}