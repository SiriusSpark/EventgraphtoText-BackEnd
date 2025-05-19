package com.example.myapp.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeepseekService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey = "z00xKMbmardDq7DfWhhfmRAVEwe3WKEqLRFQBbH3O3sNbzKB";
    private final String apiUrl = "https://tbnx.plus7.plus/v1/chat/completions";

    public DeepseekService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 调用 DeepSeek 的聊天模型 API
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return 模型生成的回复内容
     */
    public String generateText(String systemPrompt, String userMessage) {
        try {
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            // 添加系统提示词
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                Map<String, String> systemMessage = new HashMap<>();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messages.add(systemMessage);
            }
            
            // 添加用户消息
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");
            requestBody.put("messages", messages);
            requestBody.put("stream", false);

            // 发送请求
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            // 解析响应
            DeepseekResponse deepseekResponse = objectMapper.readValue(response.getBody(), DeepseekResponse.class);
            
            // 检查响应是否包含有效的回复
            if (deepseekResponse != null && 
                deepseekResponse.getChoices() != null && 
                !deepseekResponse.getChoices().isEmpty() && 
                deepseekResponse.getChoices().get(0).getMessage() != null) {
                
                return deepseekResponse.getChoices().get(0).getMessage().getContent();
            }
            
            return "无法获取有效回复";
        } catch (Exception e) {
            e.printStackTrace();
            return "调用 API 时发生错误: " + e.getMessage();
        }
    }

    @Data
    private static class DeepseekResponse {
        private String id;
        private String object;
        private long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    private static class Choice {
        private Message message;
        @JsonProperty("finish_reason")
        private String finishReason;
        private int index;
    }

    @Data
    private static class Message {
        private String role;
        private String content;
    }

    @Data
    private static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
} 