package com.example.myapp.mapper;

import com.example.myapp.entity.EventGraph;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EventGraphMapper {
    @Results(id = "eventGraphMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "userId", column = "user_id"),
        @Result(property = "title", column = "title"),
        @Result(property = "description", column = "description"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    @Select("SELECT * FROM event_graphs WHERE id = #{id}")
    EventGraph findById(Long id);

    @ResultMap("eventGraphMap")
    @Select("SELECT * FROM event_graphs WHERE user_id = #{userId}")
    List<EventGraph> findByUserId(Long userId);

    @Insert("INSERT INTO event_graphs (user_id, title, description) " +
            "VALUES (#{userId}, #{title}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EventGraph eventGraph);

    @Update("UPDATE event_graphs SET title = #{title}, description = #{description} " +
            "WHERE id = #{id} AND user_id = #{userId}")
    int update(EventGraph eventGraph);

    @Delete("DELETE FROM event_graphs WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
} 