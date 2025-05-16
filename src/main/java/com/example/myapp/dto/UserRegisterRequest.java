package com.example.myapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String nickname;
    private String password;
    private String email;
    @JsonProperty("avatar_url")
    private String avatarUrl;
}