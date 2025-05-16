package com.example.myapp.service;

import com.example.myapp.entity.GeneratedText;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

public interface GeneratedTextService {
    // 根据事件图和风格生成文本
    GeneratedText generateText(Long userId, Long eventGraphId, Long styleId);

    // 新增：带自定义提示词的生成文本方法
    GeneratedText generateText(Long userId, Long eventGraphId, Long styleId, String customPrompt);

    // 获取指定ID的文本
    GeneratedText getTextById(Long id);

    // 更新文本内容
    GeneratedText updateTextContent(Long id, Long userId, String content);

    // 删除文本
    void deleteText(Long id, Long userId);

    // 获取指定事件图的最新文本
    Optional<GeneratedText> getLatestTextByEventGraphId(Long eventGraphId);

    // 获取指定事件图的所有文本
    List<GeneratedText> getAllTextsByEventGraphId(Long eventGraphId);

    // 获取用户的所有文本
    List<GeneratedText> getAllTextsByUserId(Long userId);

    // 导出单个文本到输出流
    void exportText(Long id, OutputStream outputStream);

    // 导出指定事件图的所有文本到输出流（ZIP格式）
    void exportTextsByEventGraphId(Long eventGraphId, OutputStream outputStream);

    // 导出用户的所有文本到输出流（ZIP格式）
    void exportAllTextsByUserId(Long userId, OutputStream outputStream);
}