package com.example.myapp.config;

import com.example.myapp.util.JwtUtil;
import com.example.myapp.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("请求方法: " + request.getMethod());

        // 检查是否是不需要验证的路径
        String path = request.getRequestURI();
        // 登录、注册和获取所有用户列表接口不需要token
        if (path.endsWith("/register") || path.endsWith("/login") || 
            (path.equals("/api/users") && request.getMethod().equals("GET")) ||
            path.startsWith("/uploads/avatars/")) {
            System.out.println("不需要验证的路径");
            return true;
        }

        // 获取Authorization header
        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + authHeader);

        // 验证Authorization header格式
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Token未提供或格式错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 提取token
        String token = authHeader.substring(7);
        System.out.println("提取到的token: " + token);

        // 验证token
        if (!jwtUtil.validateToken(token)) {
            System.out.println("Token验证失败");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            // 将用户ID添加到请求属性中和UserContext中
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("Token验证成功，用户ID: " + userId);
            request.setAttribute("userId", userId);
            UserContext.setCurrentUserId(userId);
            return true;
        } catch (Exception e) {
            System.out.println("Token处理异常: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex) {
        // 清理 UserContext
        UserContext.clear();
    }
} 