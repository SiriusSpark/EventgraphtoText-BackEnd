package com.example.myapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("用户管理系统 API")
                        .description("基于 Spring Boot 的用户管理系统，支持 MySQL 和 Neo4j 数据库")
                        .version("1.0")
                        .contact(new Contact()
                                .name("开发团队")
                                .email("support@example.com")));
    }
} 