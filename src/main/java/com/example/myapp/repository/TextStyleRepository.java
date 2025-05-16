package com.example.myapp.repository;

import com.example.myapp.entity.TextStyle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TextStyleRepository extends JpaRepository<TextStyle, Long> {
    List<TextStyle> findByUserIdIsNullOrUserId(Long userId);
    Optional<TextStyle> findByUserIdAndName(Long userId, String name);
    boolean existsByUserIdAndName(Long userId, String name);
} 