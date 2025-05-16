package com.example.myapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {
        private final AuthInterceptor authInterceptor;

        public WebConfig(AuthInterceptor authInterceptor) {
                this.authInterceptor = authInterceptor;
        }

        @Override
        public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                                .allowedOrigins("http://localhost:3000", "http://localhost:5173",
                                                "http://127.0.0.1:5173",
                                                "http://127.0.0.1:3000")
                                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                                .allowedHeaders("*")
                                .exposedHeaders("Content-Disposition", "Content-Type", "Content-Length")
                                .allowCredentials(true)
                                .maxAge(3600); // 1小时预检请求缓存
        }

        @Override
        public void addInterceptors(@NonNull InterceptorRegistry registry) {
                registry.addInterceptor(authInterceptor)
                                .addPathPatterns("/api/**")
                                .excludePathPatterns(
                                                "/api/users/register",
                                                "/api/users/login",
                                                "/uploads/avatars/**",
                                                "/api/v3/api-docs/**",
                                                "/swagger-ui/**");
        }

        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                // 配置头像文件的访问路径 - 使用绝对路径
                registry.addResourceHandler("/uploads/avatars/**")
                                .addResourceLocations("file:./uploads/avatars/");

                // Swagger UI 资源路径
                registry.addResourceHandler("/swagger-ui/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
                                .resourceChain(false);
        }
}