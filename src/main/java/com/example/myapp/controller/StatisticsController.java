package com.example.myapp.controller;

import com.example.myapp.common.ApiResponse;
import com.example.myapp.dto.SystemStatisticsDTO;
import com.example.myapp.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统统计", description = "系统数据统计相关的操作")
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "系统数据统计", description = "获取系统中用户、事件图、文本风格和生成文本的总数")
    @GetMapping("/system")
    public ResponseEntity<ApiResponse<SystemStatisticsDTO>> getSystemStatistics() {
        try {
            SystemStatisticsDTO statistics = statisticsService.getSystemStatistics();
            return ResponseEntity.ok(ApiResponse.success("获取系统统计数据成功", statistics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取系统统计数据失败: " + e.getMessage()));
        }
    }
}