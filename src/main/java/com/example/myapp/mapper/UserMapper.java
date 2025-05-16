package com.example.myapp.mapper;

import com.example.myapp.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Results(id = "userMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "nickname", column = "nickname"),
        @Result(property = "password", column = "password"),
        @Result(property = "email", column = "email"),
        @Result(property = "avatarUrl", column = "avatar_url"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "lastActiveAt", column = "last_active_at")
    })
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    @ResultMap("userMap")
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @ResultMap("userMap")
    @Select("SELECT * FROM users")
    List<User> findAll();

    @Insert("INSERT INTO users (username, nickname, password, email, avatar_url, created_at, last_active_at) " +
            "VALUES (#{username}, #{nickname}, #{password}, #{email}, #{avatarUrl}, #{createdAt}, #{lastActiveAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("<script>" +
            "UPDATE users" +
            "<set>" +
            "<if test='nickname != null'>nickname = #{nickname},</if>" +
            "<if test='password != null'>password = #{password},</if>" +
            "<if test='email != null'>email = #{email},</if>" +
            "<if test='avatarUrl != null'>avatar_url = #{avatarUrl},</if>" +
            "<if test='lastActiveAt != null'>last_active_at = #{lastActiveAt}</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(User user);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE users SET password = #{newPassword} WHERE id = #{id} AND password = #{oldPassword}")
    int updatePassword(@Param("id") Long id, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);
} 