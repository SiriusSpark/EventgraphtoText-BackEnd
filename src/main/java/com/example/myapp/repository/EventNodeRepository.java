package com.example.myapp.repository;

import com.example.myapp.entity.EventNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;
import java.util.Optional;

@Repository
public interface EventNodeRepository extends Neo4jRepository<EventNode, String> {
    
    @Query("""
        MATCH (n:Event) 
        WHERE n.eventGraphId = $eventGraphId 
        RETURN n {
            .*, 
            id: elementId(n)
        } AS node
    """)
    List<EventNode> findByEventGraphId(@Param("eventGraphId") Long eventGraphId);
    
    @Query("MATCH (n:Event) WHERE n.eventGraphId = $eventGraphId DELETE n")
    void deleteByEventGraphId(@Param("eventGraphId") Long eventGraphId);

    @Query("""
        CREATE (n:Event {
            title: $title,
            description: $description,
            location: $location,
            startTime: $startTime,
            endTime: $endTime,
            eventGraphId: $eventGraphId,
            labels: $labels
        })
        WITH n
        RETURN n {
            .*,
            id: elementId(n)
        } AS node
    """)
    EventNode createNode(
        @Param("title") String title,
        @Param("description") String description,
        @Param("location") String location,
        @Param("startTime") String startTime,
        @Param("endTime") String endTime,
        @Param("eventGraphId") Long eventGraphId,
        @Param("labels") Set<String> labels
    );

    @Query("""
        MATCH (n:Event) 
        WHERE elementId(n) = $id 
        RETURN n {
            title: n.title,
            description: n.description,
            location: n.location,
            startTime: n.startTime,
            endTime: n.endTime,
            eventGraphId: n.eventGraphId,
            labels: n.labels,
            id: elementId(n)
        }
    """)
    EventNode findNodeById(@NonNull @Param("id") String id);

    @Override
    @NonNull
    @Query("""
        MATCH (n:Event) 
        WHERE elementId(n) = $id 
        RETURN n {
            title: n.title,
            description: n.description,
            location: n.location,
            startTime: n.startTime,
            endTime: n.endTime,
            eventGraphId: n.eventGraphId,
            labels: n.labels,
            id: elementId(n)
        }
    """)
    Optional<EventNode> findById(@NonNull String id);

    @Override
    @Query("""
        MATCH (n:Event)
        WHERE elementId(n) = $id
        DETACH DELETE n
    """)
    void deleteById(@NonNull String id);

    @Override
    @Query("""
        MATCH (n:Event)
        WHERE elementId(n) = $id
        RETURN COUNT(n) > 0
    """)
    boolean existsById(@NonNull String id);

    @Query("""
        MATCH (n:Event)
        WHERE elementId(n) = $id
        DETACH DELETE n
        RETURN COUNT(n) AS deletedCount
    """)
    long deleteNodeById(@NonNull @Param("id") String id);

    @Query("""
        MATCH (n:Event)
        WHERE n.eventGraphId = $eventGraphId
        RETURN n {
            .*,
            id: elementId(n)
        } AS n
    """)
    EventNode findNodeByEventGraphId(@Param("eventGraphId") Long eventGraphId);

    @Query("""
        MATCH (n:Event)
        WHERE n.eventGraphId = $graphId AND split(elementId(n), ':')[2] = toString($nodeId)
        RETURN n {
            .*,
            id: elementId(n)
        } AS n
    """)
    EventNode findByEventGraphIdAndNodeId(
        @Param("graphId") Long graphId,
        @Param("nodeId") Long nodeId
    );

    @Query("""
        MATCH (n:Event)
        WHERE n.eventGraphId = $graphId AND n.businessId = $nodeId
        RETURN n {
            .*,
            id: elementId(n)
        } AS n
    """)
    EventNode findByEventGraphIdAndBusinessId(
        @Param("graphId") Long graphId,
        @Param("nodeId") Long nodeId
    );
} 