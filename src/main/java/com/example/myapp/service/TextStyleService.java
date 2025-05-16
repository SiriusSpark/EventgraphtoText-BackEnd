package com.example.myapp.service;

import com.example.myapp.entity.TextStyle;
import com.example.myapp.repository.TextStyleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TextStyleService {
    @Autowired
    private TextStyleRepository textStyleRepository;

    public List<TextStyle> getAllStyles(Long userId) {
        return textStyleRepository.findByUserIdIsNullOrUserId(userId);
    }

    public TextStyle getStyleById(Long id) {
        return textStyleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("无效的风格 ID"));
    }

    @Transactional
    public TextStyle createStyle(TextStyle style, Long userId) {
        if (textStyleRepository.existsByUserIdAndName(userId, style.getName())) {
            throw new RuntimeException("风格名已存在，请选择其他名称");
        }
        style.setUserId(userId);
        return textStyleRepository.save(style);
    }

    @Transactional
    public TextStyle updateStyle(Long id, TextStyle updatedStyle, Long userId) {
        TextStyle existingStyle = textStyleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("无效的风格 ID"));
        
        if (existingStyle.getUserId() == null) {
            throw new RuntimeException("不能修改系统预设风格");
        }
        
        if (!existingStyle.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此风格");
        }

        // 部分更新：只有当提供了新的名称时才更新
        if (StringUtils.hasText(updatedStyle.getName())) {
            if (!existingStyle.getName().equals(updatedStyle.getName()) &&
                textStyleRepository.existsByUserIdAndName(userId, updatedStyle.getName())) {
                throw new RuntimeException("风格名已存在，请选择其他名称");
            }
            existingStyle.setName(updatedStyle.getName());
        }

        // 部分更新：只有当提供了新的描述时才更新
        if (updatedStyle.getDescription() != null) {
            existingStyle.setDescription(updatedStyle.getDescription());
        }

        return textStyleRepository.save(existingStyle);
    }

    @Transactional
    public void deleteStyle(Long id, Long userId) {
        TextStyle style = textStyleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("无效的风格 ID"));
        
        if (style.getUserId() == null) {
            throw new RuntimeException("不能删除系统预设风格");
        }
        
        if (!style.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此风格");
        }

        textStyleRepository.deleteById(id);
    }
} 