package com.example.myapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "生成的文本")
@Data
@Entity
@Table(name = "generated_texts")
public class GeneratedText {
    @Schema(description = "文本ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "用户ID")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Schema(description = "事件图ID")
    @Column(name = "event_graph_id", nullable = false)
    private Long eventGraphId;

    @Schema(description = "风格ID")
    @Column(name = "style_id")
    private Long styleId;

    @Schema(description = "文本内容")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Schema(description = "创建时间")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 