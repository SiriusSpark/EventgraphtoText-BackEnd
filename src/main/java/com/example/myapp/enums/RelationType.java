package com.example.myapp.enums;

public enum RelationType {
    // 时间关系
    PRECEDES,      // 先于
    FOLLOWS,        // 时序关系：跟随
    CONCURRENT_WITH,// 同时

    // 因果关系
    CAUSES,         // 因果关系：导致
    RESULTS_IN,     // 结果

    // 其他关系
    COREFERS_TO,    // 共指关系：指代
    HAS_SUBEVENT,   // 包含子事件
    SUBEVENT_OF,    // 子事件关系：属于
    SIMILAR_TO       // 相似关系：类似
} 