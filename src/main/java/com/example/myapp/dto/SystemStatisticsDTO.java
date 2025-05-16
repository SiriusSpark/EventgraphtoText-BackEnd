package com.example.myapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatisticsDTO {
    private int userCount;
    private int eventGraphCount;
    private int textStyleCount;
    private int generatedTextCount;
}