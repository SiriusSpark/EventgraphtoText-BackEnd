package com.example.myapp.dto;

import lombok.Data;
import java.util.List;

/**
 * 事件图完整数据DTO，包含所有节点和关系
 */
@Data
public class EventGraphDataDTO {
    private Long eventGraphId;
    private String title;
    private String description;
    private List<EventNodeDTO> nodes;
    private List<EventRelationDTO> edges;
} 