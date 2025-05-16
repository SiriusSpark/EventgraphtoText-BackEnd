package com.example.myapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    @NotNull
    private Long id;
    @NotNull
    private String username;
    private String nickname;
    private String email;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("last_active_at")
    private LocalDateTime lastActiveAt;
} 