package com.example.myapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
        private final AuthInterceptor authInterceptor;

        public WebConfig(AuthInterceptor authInterceptor) {
                this.authInterceptor = authInterceptor;
        }

        // 注释掉原有的CORS配置，使用SecurityConfig中的配置
        /*
         * @Override
         * public void addCorsMappings(@NonNull CorsRegistry registry) {
         * registry.addMapping("/**")
         * .allowedOrigins(
         * // 本地开发环境
         * "http://localhost:3000",
         * "http://localhost:5173",
         * "http://127.0.0.1:5173",
         * "http://127.0.0.1:3000",
         * // Railway云端环境 - 根据实际域名修改
         * "https://eventgraphtotext-frontend.up.railway.app",
         * "https://eventgraphtotext-frontend.railway.app",
         * "https://eventgraphtotext-frontend-production.up.railway.app",
         * // 添加"*"可以允许所有域名访问，但在生产环境中可能不够安全
         * "*")
         * .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
         * .allowedHeaders("*")
         * .exposedHeaders("Content-Disposition", "Content-Type", "Content-Length")
         * .allowCredentials(true)
         * .maxAge(3600); // 1小时预检请求缓存
         * }
         */

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

                // 配置前端静态资源访问
                registry.addResourceHandler("/**")
                                .addResourceLocations("classpath:/static/")
                                .resourceChain(true);
        }
}