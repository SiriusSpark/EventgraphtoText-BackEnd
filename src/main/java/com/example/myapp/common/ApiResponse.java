package com.example.myapp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setError(null);
        return response;
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> success(String message) {
        Map<String, Object> emptyData = new HashMap<>();
        return (ApiResponse<T>) success(message, emptyData);
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("");
        Map<String, Object> emptyData = new HashMap<>();
        response.setData((T) emptyData);
        response.setError(error);
        return response;
    }
} 