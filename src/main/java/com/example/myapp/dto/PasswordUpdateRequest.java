package com.example.myapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
 
@Data
public class PasswordUpdateRequest {
    @JsonProperty("old_password")
    private String oldPassword;
    @JsonProperty("new_password")
    private String newPassword;
} 