package com.example.myapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
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
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**") // 所有路径
                        .allowedOrigins(
                                // 本地开发环境
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "http://127.0.0.1:5173",
                                "http://127.0.0.1:3000",
                                // Railway云端环境 - 替换为您实际的前端域名
                                "https://your-frontend-app.up.railway.app")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")
                        .exposedHeaders("Content-Disposition", "Content-Type", "Content-Length")
                        .allowCredentials(true)
                        .maxAge(3600); // 预检请求缓存时间
            }
        };
    }
}