package com.example.myapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@MapperScan("com.example.myapp.mapper")
public class MyAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyAppApplication.class, args);
    }

    /**
     * 全局CORS配置 - 应用级别
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // 所有路径
                        .allowedOriginPatterns("*") // 所有源
                        .allowedMethods("*") // 所有HTTP方法
                        .allowedHeaders("*") // 所有请求头
                        .allowCredentials(true) // 允许凭证
                        .maxAge(3600); // 预检请求缓存时间
            }
        };
    }
}