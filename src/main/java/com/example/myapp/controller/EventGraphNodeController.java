package com.example.myapp.controller;

import com.example.myapp.common.ApiResponse;
import com.example.myapp.dto.EventNodeDTO;
import com.example.myapp.dto.EventRelationDTO;
import com.example.myapp.dto.EventGraphDataDTO;
import com.example.myapp.service.EventGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@Tag(name = "事件图节点和关系", description = "管理事件图中的节点和关系（Neo4j操作）")
@RestController
@RequestMapping("/api/graph")
public class EventGraphNodeController {
    private final EventGraphService eventGraphService;

    public EventGraphNodeController(EventGraphService eventGraphService) {
        this.eventGraphService = eventGraphService;
    }

    // 获取事件图完整数据
    @Operation(summary = "获取事件图的所有信息", description = "获取指定事件图的所有节点和关系")
    @GetMapping("/{eventGraphId}/data")
    public ResponseEntity<ApiResponse<EventGraphDataDTO>> getEventGraphData(
            @Parameter(description = "事件图ID") @PathVariable Long eventGraphId) {
        try {
            EventGraphDataDTO graphData = eventGraphService.getEventGraphData(eventGraphId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", graphData));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    // 节点操作接口
    @Operation(summary = "创建事件节点", description = "在事件图中创建一个新的事件节点")
    @PostMapping("/nodes")
    public ResponseEntity<ApiResponse<EventNodeDTO>> createNode(
            @RequestBody EventNodeDTO nodeDTO,
            HttpServletRequest request) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("未登录或登录已过期"));
            }
            EventNodeDTO created = eventGraphService.createNode(nodeDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("节点创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "获取事件节点", description = "获取指定ID的事件节点详情")
    @GetMapping("/nodes/{id}")
    public ResponseEntity<ApiResponse<EventNodeDTO>> getNode(
            @Parameter(description = "节点ID") @PathVariable String id) {
        try {
            // 获取完整的节点ID
            String fullNodeId = getFullNodeId(id);
            EventNodeDTO node = eventGraphService.getNode(fullNodeId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", node));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "更新事件节点", description = "更新指定ID的事件节点信息")
    @PutMapping("/nodes/{id}")
    public ResponseEntity<ApiResponse<EventNodeDTO>> updateNode(
            @Parameter(description = "节点ID") @PathVariable String id,
            @RequestBody EventNodeDTO nodeDTO) {
        try {
            // 获取完整的节点ID
            String fullNodeId = getFullNodeId(id);
            EventNodeDTO updated = eventGraphService.updateNode(fullNodeId, nodeDTO);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "删除事件节点", description = "删除指定ID的事件节点及其相关关系")
    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNode(
            @Parameter(description = "节点ID") @PathVariable String id) {
        try {
            // 获取完整的节点ID
            String fullNodeId = getFullNodeId(id);
            eventGraphService.deleteNode(fullNodeId);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取完整的节点ID
     * 如果输入的是数字，转换为完整的Neo4j节点ID格式
     */
    private String getFullNodeId(String id) {
        // 如果ID已经是完整格式（包含:），直接返回
        if (id.contains(":")) {
            return id;
        }
        try {
            // 尝试解析数字部分
            Integer.parseInt(id);
            // 格式：4:database-uuid:number
            return "4:0574c409-de78-43f2-9162-7158ae5c3692:" + id;
        } catch (NumberFormatException e) {
            // 如果不是数字，直接返回原始ID
            return id;
        }
    }

    // 关系操作接口
    @Operation(summary = "创建事件关系", description = "在两个事件节点之间创建关系")
    @PostMapping("/edges")
    public ResponseEntity<ApiResponse<EventRelationDTO>> createRelation(
            @RequestBody EventRelationDTO relationDTO) {
        try {
            EventRelationDTO created = eventGraphService.createRelation(relationDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("关系创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "获取事件关系", description = "获取指定ID的事件关系详情")
    @GetMapping("/edges/{id}")
    public ResponseEntity<ApiResponse<EventRelationDTO>> getRelation(
            @Parameter(description = "关系ID") @PathVariable String id) {
        try {
            // 获取完整的关系ID
            String fullRelationId = getFullRelationId(id);
            EventRelationDTO relation = eventGraphService.getRelation(fullRelationId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", relation));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("获取失败：" + e.getMessage()));
        }
    }

    @Operation(summary = "更新事件关系", description = "更新指定ID的事件关系信息")
    @PutMapping("/edges/{id}")
    public ResponseEntity<ApiResponse<EventRelationDTO>> updateRelation(
            @Parameter(description = "关系ID") @PathVariable String id,
            @RequestBody EventRelationDTO relationDTO) {
        try {
            // 获取完整的关系ID
            String fullRelationId = getFullRelationId(id);
            EventRelationDTO updated = eventGraphService.updateRelation(fullRelationId, relationDTO);
            return ResponseEntity.ok(ApiResponse.success("更新成功", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新失败：" + e.getMessage()));
        }
    }

    /**
     * 获取完整的关系ID
     * 如果输入的是数字，转换为完整的Neo4j关系ID格式
     */
    private String getFullRelationId(String id) {
        // 如果ID已经是完整格式（包含:），直接返回
        if (id.contains(":")) {
            return id;
        }
        try {
            // 尝试解析数字部分
            Integer.parseInt(id);
            // 格式：5:database-uuid:number (注意：关系ID以5开头，而不是4)
            return "5:0574c409-de78-43f2-9162-7158ae5c3692:" + id;
        } catch (NumberFormatException e) {
            // 如果不是数字，直接返回原始ID
            return id;
        }
    }

    @Operation(summary = "删除事件关系", description = "删除指定ID的事件关系")
    @DeleteMapping("/edges/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @Parameter(description = "关系ID") @PathVariable String id) {
        try {
            // 获取完整的关系ID
            String fullRelationId = getFullRelationId(id);
            eventGraphService.deleteRelation(fullRelationId);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除失败：" + e.getMessage()));
        }
    }
} 