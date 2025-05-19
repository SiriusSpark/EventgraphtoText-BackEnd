package com.example.myapp.entity;

import org.springframework.data.neo4j.core.schema.*;
import com.example.myapp.enums.RelationType;
import com.example.myapp.enums.RelationStrength;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.Data;

@Data
@RelationshipProperties
public class EventRelation {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;

    @Property("eventGraphId")
    private Long eventGraphId;

    @Property("sourceId")
    private String sourceId;

    @Property("targetId")
    private String targetId;

    @Property("type")
    private String type;

    @Property("strength")
    private String strength;

    @TargetNode
    private EventNode target;

    public RelationType getRelationType() {
        return type != null ? RelationType.valueOf(type) : null;
    }

    public void setRelationType(RelationType type) {
        this.type = type != null ? type.name() : null;
    }

    public RelationStrength getRelationStrength() {
        return strength != null ? RelationStrength.valueOf(strength) : null;
    }

    public void setRelationStrength(RelationStrength strength) {
        this.strength = strength != null ? strength.name() : null;
    }
}