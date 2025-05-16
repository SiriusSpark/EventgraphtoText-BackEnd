package com.example.myapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SimpleCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 创建CORS配置
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有域名
        config.addAllowedOriginPattern("*");

        // 允许凭证（cookies等）
        config.setAllowCredentials(true);

        // 允许所有请求头
        config.addAllowedHeader("*");

        // 允许所有方法（GET, POST等）
        config.addAllowedMethod("*");

        // 设置预检请求的有效期（秒）
        config.setMaxAge(3600L);

        // 允许暴露的响应头
        config.addExposedHeader("Content-Disposition");

        // 创建URL映射
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 对所有路径应用上述CORS配置
        source.registerCorsConfiguration("/**", config);

        // 创建并返回CORS过滤器
        return new CorsFilter(source);
    }
}