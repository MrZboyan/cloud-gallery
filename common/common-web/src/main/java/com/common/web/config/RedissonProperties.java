package com.common.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.data.redis")
public class RedissonProperties {

    private String host;

    private Integer port;

    private Integer database;

    private String password;

}