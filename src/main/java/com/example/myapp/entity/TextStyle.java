package com.example.myapp.entity;

import lombok.Data;
import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Schema(description = "文本风格")
@Data
@Entity
@Table(name = "text_styles")
public class TextStyle {
    @Schema(description = "风格ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "user_id")
    private Long userId;

    @Schema(description = "风格名称", example = "学术")
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Schema(description = "风格描述", example = "适用于学术论文的写作风格")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
} 