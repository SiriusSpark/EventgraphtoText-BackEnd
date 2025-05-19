package com.example.myapp.repository;

import com.example.myapp.entity.GeneratedText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneratedTextRepository extends JpaRepository<GeneratedText, Long> {
    List<GeneratedText> findByEventGraphIdOrderByCreatedAtDesc(Long eventGraphId);
    
    List<GeneratedText> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT g FROM GeneratedText g WHERE g.eventGraphId = :eventGraphId ORDER BY g.createdAt DESC")
    List<GeneratedText> findAllByEventGraphIdOrderedByCreatedAtDesc(Long eventGraphId);
    
    List<GeneratedText> findByEventGraphIdOrderByCreatedAtDesc(Long eventGraphId, Pageable pageable);
    
    default Optional<GeneratedText> findLatestByEventGraphId(Long eventGraphId) {
        List<GeneratedText> texts = findByEventGraphIdOrderByCreatedAtDesc(
            eventGraphId, 
            org.springframework.data.domain.PageRequest.of(0, 1)
        );
        return texts.isEmpty() ? Optional.empty() : Optional.of(texts.get(0));
    }
    
    List<GeneratedText> findByUserIdAndEventGraphIdOrderByCreatedAtDesc(Long userId, Long eventGraphId);
} 