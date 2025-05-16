package com.example.myapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(id) FROM users")
    int countUsers();

    @Select("SELECT COUNT(id) FROM event_graphs")
    int countEventGraphs();

    @Select("SELECT COUNT(id) FROM text_styles")
    int countTextStyles();

    @Select("SELECT COUNT(id) FROM generated_texts")
    int countGeneratedTexts();
}