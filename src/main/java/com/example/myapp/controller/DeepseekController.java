package com.example.myapp.controller;

import com.example.myapp.service.DeepseekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DeepSeek API", description = "调用 DeepSeek 模型 API")
@RestController
@RequestMapping("/api/deepseek")
public class DeepseekController {

    @Autowired
    private DeepseekService deepseekService;

    @Operation(summary = "生成文本", description = "调用 DeepSeek API 生成文本")
    @PostMapping("/generate")
    public ResponseEntity<GenerateResponse> generateText(@RequestBody GenerateRequest request) {
        String response = deepseekService.generateText(request.getSystemPrompt(), request.getUserMessage());
        return ResponseEntity.ok(new GenerateResponse(response));
    }

    @Data
    public static class GenerateRequest {
        private String systemPrompt;
        private String userMessage;
    }

    @Data
    public static class GenerateResponse {
        private String content;

        public GenerateResponse(String content) {
            this.content = content;
        }
    }
} 