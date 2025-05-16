package com.example.myapp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventNodeDTO {
    private String id;
    private Long eventGraphId;
    private String title;
    private String description;
    private String location;
    private java.util.Set<String> labels;

    private String startTime;
    private String endTime;
} 