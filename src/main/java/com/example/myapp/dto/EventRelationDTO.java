package com.example.myapp.dto;

import com.example.myapp.enums.RelationType;
import com.example.myapp.enums.RelationStrength;
import lombok.Data;

@Data
public class EventRelationDTO {
    private String id;
    private Long eventGraphId;
    private String sourceId;
    private String targetId;
    private RelationType type;
    private RelationStrength strength;
} 