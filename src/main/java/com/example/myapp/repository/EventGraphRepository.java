package com.example.myapp.repository;

import com.example.myapp.entity.EventGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventGraphRepository extends JpaRepository<EventGraph, Long> {
    List<EventGraph> findByUserId(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
} 