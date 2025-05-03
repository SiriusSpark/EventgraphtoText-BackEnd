package com.example.myapp.service.impl;

import com.example.myapp.dto.EventNodeDTO;
import com.example.myapp.dto.EventRelationDTO;
import com.example.myapp.dto.EventGraphDataDTO;
import com.example.myapp.entity.EventNode;
import com.example.myapp.entity.EventRelation;
import com.example.myapp.entity.EventGraph;
import com.example.myapp.repository.EventNodeRepository;
import com.example.myapp.repository.EventRelationRepository;
import com.example.myapp.repository.EventGraphRepository;
import com.example.myapp.service.EventGraphService;
import com.example.myapp.enums.RelationType;
import com.example.myapp.enums.RelationStrength;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class EventGraphServiceImpl implements EventGraphService {
    private final EventNodeRepository nodeRepository;
    private final EventRelationRepository relationRepository;
    private final EventGraphRepository eventGraphRepository;

    public EventGraphServiceImpl(
            EventNodeRepository nodeRepository,
            EventRelationRepository relationRepository,
            EventGraphRepository eventGraphRepository) {
        this.nodeRepository = nodeRepository;
        this.relationRepository = relationRepository;
        this.eventGraphRepository = eventGraphRepository;
    }

    // Event Graph operations
    @Override
    @Transactional
    public EventGraph createEventGraph(Long userId, String title, String description) {
        EventGraph eventGraph = new EventGraph();
        eventGraph.setUserId(userId);
        eventGraph.setTitle(title);
        eventGraph.setDescription(description);
        eventGraph.setCreatedAt(LocalDateTime.now());
        eventGraph.setUpdatedAt(LocalDateTime.now());
        return eventGraphRepository.save(eventGraph);
    }

    @Override
    public List<EventGraph> getUserEventGraphs(Long userId) {
        return eventGraphRepository.findByUserId(userId);
    }

    @Override
    public EventGraph getEventGraph(Long id, Long userId) {
        return eventGraphRepository.findById(id)
                .filter(graph -> graph.getUserId().equals(userId))
                .orElse(null);
    }

    @Override
    @Transactional
    public EventGraph updateEventGraph(Long id, Long userId, String title, String description) {
        EventGraph eventGraph = getEventGraph(id, userId);
        if (eventGraph == null) {
            return null;
        }
        eventGraph.setTitle(title);
        eventGraph.setDescription(description);
        eventGraph.setUpdatedAt(LocalDateTime.now());
        return eventGraphRepository.save(eventGraph);
    }

    @Override
    @Transactional
    public void deleteEventGraph(Long id, Long userId) {
        EventGraph eventGraph = getEventGraph(id, userId);
        if (eventGraph != null) {
            // Delete all nodes and relations in this graph
            relationRepository.deleteByEventGraphId(id);
            nodeRepository.deleteByEventGraphId(id);
            eventGraphRepository.deleteByIdAndUserId(id, userId);
        }
    }

    // Node operations
    @Override
    @Transactional
    public EventNodeDTO createNode(EventNodeDTO dto) {
        // Create node using flattened parameters
        EventNode savedNode = nodeRepository.createNode(
            dto.getTitle(),
            dto.getDescription(),
            dto.getLocation(),
            dto.getStartTime(),
            dto.getEndTime(),
            dto.getEventGraphId(),
            dto.getLabels()
        );
        
        if (savedNode == null) {
            throw new RuntimeException("Failed to create node");
        }
        
        // Create result DTO
        EventNodeDTO resultDto = new EventNodeDTO();
        BeanUtils.copyProperties(savedNode, resultDto);
        return resultDto;
    }

    @Override
    public EventNodeDTO getNode(String id) {
        EventNode node = nodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        EventNodeDTO dto = new EventNodeDTO();
        BeanUtils.copyProperties(node, dto);
        // 只保留ID的最后一个数字
        String[] idParts = node.getId().split(":");
        dto.setId(idParts[idParts.length - 1]);
        return dto;
    }

    @Override
    @Transactional
    public EventNodeDTO updateNode(String id, EventNodeDTO dto) {
        EventNode node = nodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        
        // 只更新非null字段
        if (dto.getTitle() != null) {
            node.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            node.setDescription(dto.getDescription());
        }
        if (dto.getLocation() != null) {
            node.setLocation(dto.getLocation());
        }
        if (dto.getStartTime() != null) {
            node.setStartTimeFromString(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            node.setEndTimeFromString(dto.getEndTime());
        }
        if (dto.getLabels() != null) {
            node.setLabels(dto.getLabels());
        }
        if (dto.getEventGraphId() != null) {
            node.setEventGraphId(dto.getEventGraphId());
        }

        node = nodeRepository.save(node);
        EventNodeDTO resultDto = new EventNodeDTO();
        BeanUtils.copyProperties(node, resultDto);
        return resultDto;
    }

    @Override
    @Transactional
    public void deleteNode(String id) {
        // First verify the node exists
        if (!nodeRepository.existsById(id)) {
            throw new RuntimeException("Node not found with id: " + id);
        }
        
        // Delete the node and all its relationships
        long deletedCount = nodeRepository.deleteNodeById(id);
        if (deletedCount == 0) {
            throw new RuntimeException("Failed to delete node with id: " + id);
        }
    }

    @Override
    public List<EventNodeDTO> getNodesByEventGraphId(Long eventGraphId) {
        return nodeRepository.findByEventGraphId(eventGraphId).stream()
                .map(node -> {
                    EventNodeDTO dto = new EventNodeDTO();
                    BeanUtils.copyProperties(node, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Relation operations
    @Override
    @Transactional
    public EventRelationDTO createRelation(EventRelationDTO dto) {
        // 验证必要字段
        if (dto.getEventGraphId() == null || dto.getSourceId() == null || 
            dto.getTargetId() == null || dto.getType() == null) {
            throw new IllegalArgumentException("必要字段不能为空");
        }

        // 验证如果关系类型需要strength，则strength不能为空
        if (needsStrength(dto.getType())) {
            if (dto.getStrength() == null) {
                throw new IllegalArgumentException("因果关系类型需要指定strength属性");
        }
        } else {
            // 如果关系类型不需要strength，则将其设置为null
            dto.setStrength(null);
        }

        // 转换ID为Long类型
        Long sourceNodeId;
        Long targetNodeId;
        try {
            sourceNodeId = Long.parseLong(dto.getSourceId());
            targetNodeId = Long.parseLong(dto.getTargetId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("sourceId和targetId必须是数字");
        }

        // 查找源节点和目标节点，获取它们的elementId
        EventNode sourceNode = nodeRepository.findByEventGraphIdAndNodeId(dto.getEventGraphId(), sourceNodeId);
        if (sourceNode == null) {
            throw new IllegalArgumentException("源节点不存在: " + sourceNodeId);
        }
        String sourceElementId = sourceNode.getId();

        EventNode targetNode = nodeRepository.findByEventGraphIdAndNodeId(dto.getEventGraphId(), targetNodeId);
        if (targetNode == null) {
            throw new IllegalArgumentException("目标节点不存在: " + targetNodeId);
        }
        String targetElementId = targetNode.getId();

        // 根据关系类型决定是否需要包含strength属性
        EventRelation relation;
        try {
            if (needsStrength(dto.getType())) {
                relation = relationRepository.createRelationWithStrength(
                    sourceElementId,
                    targetElementId,
                    dto.getEventGraphId(),
                    dto.getType().name(),
                    dto.getStrength().name()
                );
            } else {
                relation = relationRepository.createRelationWithoutStrength(
                    sourceElementId,
                    targetElementId,
                    dto.getEventGraphId(),
                    dto.getType().name()
                );
            }
            
            if (relation == null) {
                throw new RuntimeException("创建关系失败");
            }

            // 创建新的DTO并手动设置所有字段
            EventRelationDTO resultDto = new EventRelationDTO();
            resultDto.setId(relation.getId());
            resultDto.setEventGraphId(relation.getEventGraphId());
            resultDto.setSourceId(relation.getSourceId());
            resultDto.setTargetId(relation.getTargetId());
            resultDto.setType(RelationType.valueOf(relation.getType()));
            if (relation.getStrength() != null) {
                resultDto.setStrength(RelationStrength.valueOf(relation.getStrength()));
            }
            
            return resultDto;
        } catch (Exception e) {
            throw new RuntimeException("创建关系失败: " + e.getMessage(), e);
        }
    }

    // 判断关系类型是否需要strength属性
    private boolean needsStrength(RelationType type) {
        return type == RelationType.CAUSES || 
               type == RelationType.RESULTS_IN;
    }

    @Override
    public EventRelationDTO getRelation(String id) {
        EventRelation relation = relationRepository.findRelationById(id);
        if (relation == null) {
            throw new RuntimeException("Relation not found");
        }
        
        // 创建新的DTO并手动设置所有字段
        EventRelationDTO dto = new EventRelationDTO();
        dto.setId(relation.getId());
        dto.setEventGraphId(relation.getEventGraphId());
        dto.setSourceId(relation.getSourceId());
        dto.setTargetId(relation.getTargetId());
        if (relation.getType() != null) {
            dto.setType(RelationType.valueOf(relation.getType()));
        }
        if (needsStrength(dto.getType()) && relation.getStrength() != null) {
            dto.setStrength(RelationStrength.valueOf(relation.getStrength()));
        }
        
        return dto;
    }

    @Override
    @Transactional
    public EventRelationDTO updateRelation(String id, EventRelationDTO dto) {
        // 首先验证关系是否存在
        EventRelation existingRelation = relationRepository.findRelationById(id);
        if (existingRelation == null) {
            throw new RuntimeException("Relation not found with id: " + id);
        }
        
        try {
            // 验证如果关系类型需要strength，则strength不能为空
            if (dto.getType() != null && needsStrength(dto.getType())) {
                if (dto.getStrength() == null) {
                    throw new IllegalArgumentException("因果关系类型需要指定strength属性");
                }
            }

            // 使用现有值作为默认值
            Long eventGraphId = dto.getEventGraphId() != null ? dto.getEventGraphId() : existingRelation.getEventGraphId();
            String sourceId = existingRelation.getSourceId();
            String targetId = existingRelation.getTargetId();
            
            // Get the full source and target IDs
            EventNode sourceNode = nodeRepository.findByEventGraphIdAndNodeId(eventGraphId, Long.parseLong(sourceId));
            if (sourceNode == null) {
                throw new IllegalArgumentException("源节点不存在: " + sourceId);
            }
            String sourceElementId = sourceNode.getId();
            
            EventNode targetNode = nodeRepository.findByEventGraphIdAndNodeId(eventGraphId, Long.parseLong(targetId));
            if (targetNode == null) {
                throw new IllegalArgumentException("目标节点不存在: " + targetId);
            }
            String targetElementId = targetNode.getId();
            
            String type = dto.getType() != null ? dto.getType().name() : existingRelation.getType();
            String strength = dto.getStrength() != null ? dto.getStrength().name() : existingRelation.getStrength();

            // 检查strength是否是有效的枚举值
            if (strength != null) {
                try {
                    RelationStrength.valueOf(strength);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("无效的关系强度值: " + strength);
                }
            }

            // 更新关系
            EventRelation updatedRelation = relationRepository.updateRelation(
                id,
                sourceElementId,
                targetElementId,
                eventGraphId,
                type,
                strength
            );
            
            if (updatedRelation == null) {
                throw new RuntimeException("更新关系失败");
            }

            // 创建新的DTO并手动设置所有字段
            EventRelationDTO resultDto = new EventRelationDTO();
            resultDto.setId(updatedRelation.getId());
            resultDto.setEventGraphId(updatedRelation.getEventGraphId());
            resultDto.setSourceId(updatedRelation.getSourceId());
            resultDto.setTargetId(updatedRelation.getTargetId());
            resultDto.setType(RelationType.valueOf(updatedRelation.getType()));
            if (updatedRelation.getStrength() != null) {
                resultDto.setStrength(RelationStrength.valueOf(updatedRelation.getStrength()));
            }
            
            return resultDto;
        } catch (Exception e) {
            throw new RuntimeException("更新关系失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteRelation(String id) {
        // First verify the relation exists
        EventRelation relation = relationRepository.findRelationById(id);
        if (relation == null) {
            throw new RuntimeException("关系不存在，ID: " + id);
        }
        
        // Delete the relation
        relationRepository.deleteById(id);
        
        // Verify deletion
        relation = relationRepository.findRelationById(id);
        if (relation != null) {
            throw new RuntimeException("删除关系失败，ID: " + id);
        }
    }

    @Override
    public List<EventRelationDTO> getRelationsByEventGraphId(Long eventGraphId) {
        return relationRepository.findByEventGraphId(eventGraphId).stream()
                .map(relation -> {
                    EventRelationDTO dto = new EventRelationDTO();
                    dto.setId(relation.getId());
                    dto.setEventGraphId(relation.getEventGraphId());
                    dto.setSourceId(relation.getSourceId());
                    dto.setTargetId(relation.getTargetId());
                    if (relation.getType() != null) {
                        dto.setType(RelationType.valueOf(relation.getType()));
                    }
                    if (relation.getStrength() != null) {
                        dto.setStrength(RelationStrength.valueOf(relation.getStrength()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventGraphDataDTO getEventGraphData(Long eventGraphId) {
        // 验证事件图是否存在
        EventGraph eventGraph = eventGraphRepository.findById(eventGraphId)
                .orElseThrow(() -> new RuntimeException("事件图不存在，ID: " + eventGraphId));
        
        // 获取所有节点并转换为DTO
        List<EventNodeDTO> nodes = nodeRepository.findByEventGraphId(eventGraphId).stream()
                .map(node -> {
                    EventNodeDTO dto = new EventNodeDTO();
                    BeanUtils.copyProperties(node, dto);
                    // 只保留ID的最后一个数字
                    String[] idParts = node.getId().split(":");
                    dto.setId(idParts[idParts.length - 1]);
                    return dto;
                })
                .collect(Collectors.toList());
        
        // 获取所有关系并转换为DTO
        List<EventRelationDTO> edges = relationRepository.findByEventGraphId(eventGraphId).stream()
                .map(relation -> {
                    EventRelationDTO dto = new EventRelationDTO();
                    // 只保留ID的最后一个数字
                    String[] idParts = relation.getId().split(":");
                    dto.setId(idParts[idParts.length - 1]);
                    dto.setEventGraphId(relation.getEventGraphId());
                    dto.setSourceId(relation.getSourceId());
                    dto.setTargetId(relation.getTargetId());
                    if (relation.getType() != null) {
                        dto.setType(RelationType.valueOf(relation.getType()));
                    }
                    if (relation.getStrength() != null) {
                        dto.setStrength(RelationStrength.valueOf(relation.getStrength()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
        
        // 组装结果
        EventGraphDataDTO result = new EventGraphDataDTO();
        result.setEventGraphId(eventGraphId);
        result.setTitle(eventGraph.getTitle());
        result.setDescription(eventGraph.getDescription());
        result.setNodes(nodes);
        result.setEdges(edges);
        
        return result;
    }
} 