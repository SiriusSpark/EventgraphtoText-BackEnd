package com.example.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 处理前端单页应用的路由
 * 所有非API的GET请求都重定向到index.html，由前端路由处理
 */
@Controller
public class FrontendController {

    /**
     * 处理所有未被其他控制器处理的GET请求，返回前端应用的主页
     * 
     * @return 前端应用的主页
     */
    @GetMapping(value = {
            "/",
            "/login",
            "/register",
            "/home",
            "/event-graph/**",
            "/text-generation/**",
            "/text-style/**",
            "/user/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}