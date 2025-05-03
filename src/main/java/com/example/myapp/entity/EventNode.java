package com.example.myapp.entity;

import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Node("Event")
@Data
public class EventNode {
    @Id
    @GeneratedValue(generatorClass = UUIDStringGenerator.class)
    private String id;
    
    @Property("eventGraphId")
    private Long eventGraphId;

    @Property("title")
    private String title;

    @Property("description")
    private String description;

    @Property("location")
    private String location;

    @Property("startTime")
    private String startTime;

    @Property("endTime")
    private String endTime;
    
    @Property("labels")
    private Set<String> labels = new HashSet<>();

    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.OUTGOING)
    private Set<EventRelation> outgoingRelations = new HashSet<>();

    @Relationship(type = "RELATES_TO", direction = Relationship.Direction.INCOMING)
    private Set<EventRelation> incomingRelations = new HashSet<>();

    // 添加日期时间转换方法
    public void setStartTimeFromString(String startTimeStr) {
        this.startTime = startTimeStr;
    }

    public void setEndTimeFromString(String endTimeStr) {
        this.endTime = endTimeStr;
    }

    public String getStartTimeAsString() {
        return startTime;
    }

    public String getEndTimeAsString() {
        return endTime;
    }
} 