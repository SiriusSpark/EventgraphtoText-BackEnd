package com.example.myapp.controller;

import com.example.myapp.dto.EventGraphDataDTO;
import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.GeneratedText;
import com.example.myapp.entity.TextStyle;
import com.example.myapp.service.EventGraphService;
import com.example.myapp.service.GeneratedTextService;
import com.example.myapp.service.TextStyleService;
import com.example.myapp.service.UserService;
import com.example.myapp.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "生成文本", description = "生成文本管理接口")
@RestController
@RequestMapping("/api/generated_texts")
public class GeneratedTextController {

    @Autowired
    private GeneratedTextService generatedTextService;

    @Autowired
    private TextStyleService textStyleService;

    @Autowired
    private EventGraphService eventGraphService;

    @Autowired
    private UserService userService;

    @Operation(summary = "根据事件图和风格生成文本", description = "通过指定事件图和风格生成新的文本，可选传入自定义提示词")
    @PostMapping
    public ResponseEntity<GeneratedText> generateText(@RequestBody GenerateTextRequest request) {
        Long userId = UserContext.getCurrentUserId();
        GeneratedText generatedText = generatedTextService.generateText(
                userId,
                request.getEventGraphId(),
                request.getStyleId(),
                request.getCustomPrompt());
        return ResponseEntity.ok(generatedText);
    }

    @Operation(summary = "编辑文本内容", description = "修改指定ID的文本内容")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateText(
            @Parameter(description = "文本ID") @PathVariable Long id,
            @RequestBody UpdateTextRequest request) {
        try {
            Long userId = UserContext.getCurrentUserId();
            GeneratedText updatedText = generatedTextService.updateTextContent(id, userId, request.getContent());
            return ResponseEntity.ok(Map.of(
                    "id", updatedText.getId(),
                    "updatedAt", updatedText.getUpdatedAt()));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "删除指定文本", description = "删除某条生成文本（仅限当前用户拥有的）")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteText(@Parameter(description = "文本ID") @PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            generatedTextService.deleteText(id, userId);
            return ResponseEntity.ok(Map.of("message", "文本删除成功"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "查询指定事件图的最近一条生成文本", description = "获取指定事件图的最新生成文本")
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestText(
            @Parameter(description = "事件图ID") @RequestParam Long eventGraphId) {
        Optional<GeneratedText> latestTextOpt = generatedTextService.getLatestTextByEventGraphId(eventGraphId);

        if (latestTextOpt.isPresent()) {
            GeneratedText latestText = latestTextOpt.get();
            GeneratedTextResponseDTO responseDTO = convertToResponseDTO(latestText);
            return ResponseEntity.ok(responseDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "查询指定事件图的所有文本", description = "返回按创建时间降序排序的全部文本")
    @GetMapping
    public ResponseEntity<List<GeneratedTextResponseDTO>> getAllTextsByEventGraphId(
            @Parameter(description = "事件图ID") @RequestParam Long eventGraphId) {
        List<GeneratedText> texts = generatedTextService.getAllTextsByEventGraphId(eventGraphId);
        List<GeneratedTextResponseDTO> responseDTOs = texts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "查询所有生成文本", description = "返回所有生成文本，不需要指定事件图ID")
    @GetMapping("/all")
    public ResponseEntity<List<GeneratedTextResponseDTO>> getAllTexts() {
        Long userId = UserContext.getCurrentUserId();
        List<GeneratedText> texts = generatedTextService.getAllTextsByUserId(userId);
        List<GeneratedTextResponseDTO> responseDTOs = texts.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @Operation(summary = "导出单个文本", description = "以.txt文件形式导出该文本")
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportText(
            @Parameter(description = "文本ID") @PathVariable Long id) {
        try {
            // 获取文本
            GeneratedText text = generatedTextService.getTextById(id);

            // 获取事件图标题
            EventGraphDataDTO eventGraphData = eventGraphService.getEventGraphData(text.getEventGraphId());
            String eventGraphTitle = (eventGraphData != null) ? eventGraphData.getTitle() : "未知事件图";

            // 获取风格名称（如果有的话）
            String styleName = "";
            if (text.getStyleId() != null) {
                try {
                    TextStyle style = textStyleService.getStyleById(text.getStyleId());
                    styleName = style.getName();
                } catch (Exception e) {
                    // 如果无法获取风格，就忽略
                }
            }

            // 构建文件名
            String fileName;
            if (!styleName.isEmpty()) {
                fileName = String.format("%s_%s.txt", eventGraphTitle, styleName);
            } else {
                fileName = String.format("%s.txt", eventGraphTitle);
            }

            // 处理文件名中可能含有的非法字符
            fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            generatedTextService.exportText(id, outputStream);

            // 对文件名进行URL编码，解决中文乱码问题
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // 处理空格

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            // 添加更明确的Content-Disposition设置
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            // 允许浏览器访问响应头
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("文本不存在")) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(("导出失败: " + e.getMessage()).getBytes());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("导出失败: " + e.getMessage()).getBytes());
        }
    }

    @Operation(summary = "导出指定事件图的所有文本", description = "将该图下所有文本打包导出为zip文件")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTextsByEventGraphId(
            @Parameter(description = "事件图ID") @RequestParam Long eventGraphId) {
        try {
            // 获取事件图标题
            EventGraphDataDTO eventGraphData = eventGraphService.getEventGraphData(eventGraphId);
            String eventGraphTitle = (eventGraphData != null) ? eventGraphData.getTitle() : "未知事件图";

            // 处理标题中可能含有的非法字符
            eventGraphTitle = eventGraphTitle.replaceAll("[\\\\/:*?\"<>|]", "_");

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 构建文件名: 事件图名称+导出时间
            String zipFileName = String.format("%s_%s.zip", eventGraphTitle, formattedDate);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // 创建日志记录，帮助调试
            System.out.println("开始导出事件图文本, 事件图ID: " + eventGraphId);

            try {
                generatedTextService.exportTextsByEventGraphId(eventGraphId, outputStream);
                System.out.println("事件图文本导出完成, 事件图ID: " + eventGraphId);
            } catch (Exception e) {
                System.err.println("导出事件图文本过程中出错: " + e.getMessage());
                e.printStackTrace();
                // 继续执行，尝试发送已经生成的部分内容
            }

            // 对文件名进行URL编码，解决中文乱码问题
            String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // 处理空格

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 添加更明确的Content-Disposition设置
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            // 允许浏览器访问响应头
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            // 确保无论如何都返回200状态码，让前端能正常处理
            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("导出事件图文本失败, 错误: " + e.getMessage());
            e.printStackTrace();

            // 尝试发送错误信息作为文本文件
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"error_log.txt\"");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(("导出失败: " + e.getMessage() + "\n\n请稍后再试或联系管理员。").getBytes(StandardCharsets.UTF_8));

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException ioException) {
                // 如果连错误信息也无法发送，则返回500状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(("导出失败: " + e.getMessage()).getBytes());
            }
        }
    }

    @Operation(summary = "导出用户的所有文本", description = "将当前用户所有文本统一打包导出为zip文件")
    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exportAllTexts() {
        try {
            Long userId = UserContext.getCurrentUserId();

            // 获取用户名称
            String userName = "用户";
            try {
                UserDTO userDTO = userService.getUserById(userId);
                if (userDTO != null && userDTO.getNickname() != null && !userDTO.getNickname().isEmpty()) {
                    userName = userDTO.getNickname();
                } else if (userDTO != null && userDTO.getUsername() != null && !userDTO.getUsername().isEmpty()) {
                    userName = userDTO.getUsername();
                }
            } catch (Exception e) {
                // 如果获取用户名失败，使用默认名称
                System.err.println("获取用户信息失败: " + e.getMessage());
            }

            // 处理用户名中可能含有的非法字符
            userName = userName.replaceAll("[\\\\/:*?\"<>|]", "_");

            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // 构建文件名: 用户名称_全部文本_导出时间
            String zipFileName = String.format("%s_全部文本_%s.zip", userName, formattedDate);

            // 创建日志记录，帮助调试
            System.out.println("开始导出用户所有文本, 用户ID: " + userId);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                generatedTextService.exportAllTextsByUserId(userId, outputStream);
                System.out.println("用户文本导出完成, 用户ID: " + userId);
            } catch (Exception e) {
                System.err.println("导出用户文本过程中出错: " + e.getMessage());
                e.printStackTrace();
                // 继续执行，尝试发送已经生成的部分内容
            }

            // 对文件名进行URL编码，解决中文乱码问题
            String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20"); // 处理空格

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 添加更明确的Content-Disposition设置
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
            // 允许浏览器访问响应头
            headers.add("Access-Control-Expose-Headers", "Content-Disposition");

            // 确保无论如何都返回200状态码，让前端能正常处理
            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("导出用户文本失败, 错误: " + e.getMessage());
            e.printStackTrace();

            // 尝试发送错误信息作为文本文件
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"error_log.txt\"");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(("导出失败: " + e.getMessage() + "\n\n请稍后再试或联系管理员。").getBytes(StandardCharsets.UTF_8));

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
            } catch (IOException ioException) {
                // 如果连错误信息也无法发送，则返回500状态码
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(("导出失败: " + e.getMessage()).getBytes());
            }
        }
    }

    @Operation(summary = "获取指定的生成文本", description = "根据ID获取特定的生成文本")
    @GetMapping("/{id}")
    public ResponseEntity<?> getGeneratedTextById(
            @Parameter(description = "文本ID") @PathVariable Long id) {
        try {
            // 获取文本
            GeneratedText text = generatedTextService.getTextById(id);
            GeneratedTextResponseDTO responseDTO = convertToResponseDTO(text);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "文本不存在或已被删除"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "获取文本失败: " + e.getMessage()));
        }
    }

    private GeneratedTextResponseDTO convertToResponseDTO(GeneratedText text) {
        GeneratedTextResponseDTO dto = new GeneratedTextResponseDTO();
        dto.setId(text.getId());
        dto.setEventGraphId(text.getEventGraphId());
        dto.setContent(text.getContent());
        dto.setCreatedAt(text.getCreatedAt());
        dto.setUpdatedAt(text.getUpdatedAt());

        // 设置风格信息
        if (text.getStyleId() != null) {
            TextStyle style = textStyleService.getStyleById(text.getStyleId());
            StyleDTO styleDTO = new StyleDTO();
            styleDTO.setId(style.getId());
            styleDTO.setName(style.getName());
            styleDTO.setDescription(style.getDescription());
            dto.setStyle(styleDTO);
        }

        return dto;
    }

    @Data
    public static class GenerateTextRequest {
        private Long eventGraphId;
        private Long styleId;
        private String customPrompt;
    }

    @Data
    public static class UpdateTextRequest {
        private String content;
    }

    @Data
    public static class GeneratedTextResponseDTO {
        private Long id;
        private Long eventGraphId;
        private StyleDTO style;
        private String content;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;
    }

    @Data
    public static class StyleDTO {
        private Long id;
        private String name;
        private String description;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
    }
}