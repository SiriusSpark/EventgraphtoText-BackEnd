package com.example.myapp.service;

import com.example.myapp.dto.EventNodeDTO;
import com.example.myapp.dto.EventRelationDTO;
import com.example.myapp.dto.EventGraphDataDTO;
import com.example.myapp.entity.EventGraph;

import java.util.List;

public interface EventGraphService {
    // Event Graph operations
    EventGraph createEventGraph(Long userId, String title, String description);
    List<EventGraph> getUserEventGraphs(Long userId);
    EventGraph getEventGraph(Long id, Long userId);
    EventGraph updateEventGraph(Long id, Long userId, String title, String description);
    void deleteEventGraph(Long id, Long userId);
    
    // 获取完整事件图数据（包含节点和关系）
    EventGraphDataDTO getEventGraphData(Long eventGraphId);

    // Node operations
    EventNodeDTO createNode(EventNodeDTO dto);
    EventNodeDTO getNode(String id);
    EventNodeDTO updateNode(String id, EventNodeDTO dto);
    void deleteNode(String id);
    List<EventNodeDTO> getNodesByEventGraphId(Long eventGraphId);

    // Relation operations
    EventRelationDTO createRelation(EventRelationDTO dto);
    EventRelationDTO getRelation(String id);
    EventRelationDTO updateRelation(String id, EventRelationDTO dto);
    void deleteRelation(String id);
    List<EventRelationDTO> getRelationsByEventGraphId(Long eventGraphId);
} 