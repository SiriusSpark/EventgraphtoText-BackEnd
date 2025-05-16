package com.example.myapp.controller;

import com.example.myapp.entity.TextStyle;
import com.example.myapp.service.TextStyleService;
import com.example.myapp.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "文本风格", description = "文本风格管理接口")
@RestController
@RequestMapping("/api/text_styles")
public class TextStyleController {
    @Autowired
    private TextStyleService textStyleService;

    @Operation(summary = "获取全部风格", description = "获取当前用户所有风格，包括系统预设和用户自定义的风格")
    @GetMapping
    public ResponseEntity<List<TextStyle>> getAllStyles() {
        Long userId = UserContext.getCurrentUserId();
        return ResponseEntity.ok(textStyleService.getAllStyles(userId));
    }

    @Operation(summary = "获取单个风格", description = "获取指定风格的详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<TextStyle> getStyleById(
            @Parameter(description = "风格ID") @PathVariable Long id) {
        return ResponseEntity.ok(textStyleService.getStyleById(id));
    }

    @Operation(summary = "新建自定义风格", description = "创建新的自定义风格")
    @PostMapping
    public ResponseEntity<TextStyle> createStyle(@RequestBody TextStyle style) {
        Long userId = UserContext.getCurrentUserId();
        return ResponseEntity.ok(textStyleService.createStyle(style, userId));
    }

    @Operation(summary = "修改自定义风格", description = "修改用户自定义风格的信息")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStyle(
            @Parameter(description = "风格ID") @PathVariable Long id,
            @RequestBody TextStyle style) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return ResponseEntity.ok(textStyleService.updateStyle(id, style, userId));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "删除自定义风格", description = "删除用户自定义的风格，无法删除系统预设风格")
    @ApiResponse(responseCode = "200", description = "风格删除成功")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStyle(
            @Parameter(description = "风格ID") @PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            textStyleService.deleteStyle(id, userId);
            return ResponseEntity.ok(Map.of("message", "风格删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
} 