package com.example.myapp.dto;

import lombok.Data;
 
@Data
public class UserLoginRequest {
    private String username;
    private String password;
} 