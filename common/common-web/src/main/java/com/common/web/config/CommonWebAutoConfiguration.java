package com.common.web.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Common-Web 自动配置入口
 */
@AutoConfiguration
@ComponentScan(basePackages = "com.common.web")
@Import({
        RedisAutoConfiguration.class
})
public class CommonWebAutoConfiguration {
    // 保持简洁，只做组装工作
}