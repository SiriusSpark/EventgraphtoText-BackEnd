package com.example.myapp.repository;

import com.example.myapp.entity.EventRelation;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRelationRepository extends Neo4jRepository<EventRelation, String> {
    
    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE r.eventGraphId = $eventGraphId
        RETURN r {
            .*,
            id: elementId(r),
            sourceId: split(elementId(source), ':')[2],
            targetId: split(elementId(target), ':')[2],
            type: r.type,
            strength: r.strength
        } AS r
    """)
    List<EventRelation> findByEventGraphId(@Param("eventGraphId") Long eventGraphId);
    
    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE r.eventGraphId = $eventGraphId
        DELETE r
    """)
    void deleteByEventGraphId(@Param("eventGraphId") Long eventGraphId);
    
    @Query("""
        MATCH (n:Event)-[r]-(m:Event)
        WHERE elementId(n) = $nodeId OR elementId(m) = $nodeId
        DELETE r
    """)
    void deleteByNodeId(@NonNull @Param("nodeId") String nodeId);

    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE elementId(r) = $relationId
        RETURN r {
            .*,
            id: split(elementId(r), ':')[2],
            sourceId: split(elementId(source), ':')[2],
            targetId: split(elementId(target), ':')[2],
            type: r.type,
            strength: r.strength
        } AS r
    """)
    EventRelation findRelationById(@NonNull @Param("relationId") String relationId);

    @Override
    @NonNull
    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE elementId(r) = $id
        RETURN r {
            .*,
            id: split(elementId(r), ':')[2],
            sourceId: split(elementId(source), ':')[2],
            targetId: split(elementId(target), ':')[2],
            type: r.type,
            strength: r.strength
        } AS r
    """)
    Optional<EventRelation> findById(@NonNull String id);

    @Override
    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE elementId(r) = $id
        DELETE r
    """)
    void deleteById(@NonNull String id);

    @Override
    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE elementId(r) = $id
        RETURN COUNT(r) > 0
    """)
    boolean existsById(@NonNull String id);

    @Query("""
        MATCH (source:Event)
        WHERE elementId(source) = $sourceId
        MATCH (target:Event)
        WHERE elementId(target) = $targetId
        CALL apoc.merge.relationship(
            source, 
            $type, 
            {
            eventGraphId: $eventGraphId,
            strength: $strength,
                sourceId: split($sourceId, ':')[2],
                targetId: split($targetId, ':')[2],
            type: $type
            }, 
            {},
            target
        ) YIELD rel
        RETURN rel {
            .*,
            id: elementId(rel),
            sourceId: split(elementId(source), ':')[2],
            targetId: split(elementId(target), ':')[2]
        } AS r
    """)
    EventRelation createRelationWithStrength(
        @NonNull @Param("sourceId") String sourceId,
        @NonNull @Param("targetId") String targetId,
        @NonNull @Param("eventGraphId") Long eventGraphId,
        @NonNull @Param("type") String type,
        @NonNull @Param("strength") String strength
    );

    @Query("""
        MATCH (source:Event)
        WHERE elementId(source) = $sourceId
        MATCH (target:Event)
        WHERE elementId(target) = $targetId
        CALL apoc.merge.relationship(
            source, 
            $type, 
            {
            eventGraphId: $eventGraphId,
                sourceId: split($sourceId, ':')[2],
                targetId: split($targetId, ':')[2],
            type: $type
            }, 
            {},
            target
        ) YIELD rel
        RETURN rel {
            .*,
            id: elementId(rel),
            sourceId: split(elementId(source), ':')[2],
            targetId: split(elementId(target), ':')[2]
        } AS r
    """)
    EventRelation createRelationWithoutStrength(
        @NonNull @Param("sourceId") String sourceId,
        @NonNull @Param("targetId") String targetId,
        @NonNull @Param("eventGraphId") Long eventGraphId,
        @NonNull @Param("type") String type
    );

    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE r.eventGraphId = $eventGraphId 
        AND r.sourceId = $sourceId 
        AND r.targetId = $targetId 
        AND r.type = $type
        DELETE r
    """)
    void deleteRelationByProperties(
        @Param("eventGraphId") Long eventGraphId,
        @Param("sourceId") String sourceId,
        @Param("targetId") String targetId,
        @Param("type") String type
    );

    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE r.eventGraphId = $eventGraphId 
        AND r.sourceId = $sourceId 
        AND r.targetId = $targetId 
        AND r.type = $type
        RETURN r {
            .*,
            id: elementId(r),
            sourceId: elementId(source),
            targetId: elementId(target)
        } AS r
    """)
    EventRelation findRelationByProperties(
        @Param("eventGraphId") Long eventGraphId,
        @Param("sourceId") String sourceId,
        @Param("targetId") String targetId,
        @Param("type") String type
    );

    @Query("""
        MATCH (source:Event)-[r]->(target:Event)
        WHERE elementId(r) = $id
        MATCH (newSource:Event)
        WHERE elementId(newSource) = $sourceId
        MATCH (newTarget:Event)
        WHERE elementId(newTarget) = $targetId
        CALL apoc.merge.relationship(
            newSource,
            $type,
            {
                eventGraphId: $eventGraphId,
                strength: $strength,
                sourceId: split($sourceId, ':')[2],
                targetId: split($targetId, ':')[2],
                type: $type
            },
            {},
            newTarget
        ) YIELD rel
        DELETE r
        RETURN rel {
            .*,
            id: split(elementId(rel), ':')[2],
            sourceId: split(elementId(newSource), ':')[2],
            targetId: split(elementId(newTarget), ':')[2]
        } AS r
    """)
    EventRelation updateRelation(
        @NonNull @Param("id") String id,
        @NonNull @Param("sourceId") String sourceId,
        @NonNull @Param("targetId") String targetId,
        @NonNull @Param("eventGraphId") Long eventGraphId,
        @NonNull @Param("type") String type,
        @Param("strength") String strength
    );
} 