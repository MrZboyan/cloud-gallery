package com.project.picture;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 图片服务启动类
 */
@EnableDubbo
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.project.picture.model.mapper")
public class PictureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureServiceApplication.class, args);
    }

}