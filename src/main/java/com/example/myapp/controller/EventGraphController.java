package com.example.myapp.controller;

import com.example.myapp.common.ApiResponse;
import com.example.myapp.dto.EventGraphRequest;
import com.example.myapp.entity.EventGraph;
import com.example.myapp.service.EventGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "事件图管理", description = "事件图相关的所有操作")
@RestController
@RequestMapping("/api/event_graphs")
public class EventGraphController {
    private final EventGraphService eventGraphService;

    public EventGraphController(EventGraphService eventGraphService) {
        this.eventGraphService = eventGraphService;
    }

    @Operation(summary = "创建事件图", description = "创建一个新的事件图（属于当前登录用户）")
    @PostMapping
    public ResponseEntity<ApiResponse<EventGraph>> createEventGraph(
            @RequestBody EventGraphRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("未登录或登录已过期"));
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("标题不能为空"));
            }

            EventGraph eventGraph = eventGraphService.createEventGraph(
                userId, request.getTitle(), request.getDescription());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("创建成功", eventGraph));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "获取当前用户的事件图列表", description = "获取当前登录用户创建的所有事件图")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventGraph>>> getUserEventGraphs(HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("未登录或登录已过期"));
            }

            List<EventGraph> eventGraphs = eventGraphService.getUserEventGraphs(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", eventGraphs));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "获取指定事件图详情", description = "获取某个事件图的详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGraph>> getEventGraph(
            @Parameter(description = "事件图ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("未登录或登录已过期"));
            }

            EventGraph eventGraph = eventGraphService.getEventGraph(id, userId);
            if (eventGraph == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("事件图不存在或无权访问"));
            }

            return ResponseEntity.ok(ApiResponse.success("获取成功", eventGraph));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "更新事件图信息", description = "修改事件图的标题和描述")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventGraph>> updateEventGraph(
            @Parameter(description = "事件图ID") @PathVariable Long id,
            @RequestBody EventGraphRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = (Long) httpRequest.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("未登录或登录已过期"));
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("标题不能为空"));
            }

            EventGraph eventGraph = eventGraphService.updateEventGraph(
                id, userId, request.getTitle(), request.getDescription());
            
            if (eventGraph == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("事件图不存在或无权修改"));
            }

            return ResponseEntity.ok(ApiResponse.success("更新成功", eventGraph));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "删除事件图", description = "删除一个事件图")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEventGraph(
            @Parameter(description = "事件图ID") @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("未登录或登录已过期"));
            }

            eventGraphService.deleteEventGraph(id, userId);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }
} 