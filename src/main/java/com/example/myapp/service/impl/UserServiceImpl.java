package com.example.myapp.service.impl;

import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import com.example.myapp.mapper.UserMapper;
import com.example.myapp.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.findById(id);
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userMapper.findByUsername(username);
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userMapper.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public UserDTO createUser(User user) {
        // 先检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        if (user.getAvatarUrl() == null || user.getAvatarUrl().trim().isEmpty()) {
            user.setAvatarUrl("https://example.com/avatars/default.png");
        }
        userMapper.insert(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public UserDTO updateUser(User user) {
        user.setLastActiveAt(LocalDateTime.now());
        userMapper.update(user);
        return convertToDTO(user);
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(transactionManager = "transactionManager")
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        System.out.println("开始更新密码");
        System.out.println("用户ID: " + id);
        System.out.println("旧密码: " + oldPassword);
        System.out.println("新密码: " + newPassword);
        
        // 先获取用户当前密码进行验证
        User user = userMapper.findById(id);
        if (user == null) {
            System.out.println("用户不存在");
            return false;
        }
        System.out.println("数据库中的密码: " + user.getPassword());
        
        if (!user.getPassword().equals(oldPassword)) {
            System.out.println("原密码不匹配");
            return false;
        }
        
        int result = userMapper.updatePassword(id, oldPassword, newPassword);
        System.out.println("密码更新结果: " + result);
        return result > 0;
    }

    @Override
    public boolean verifyPassword(User user) {
        User dbUser = userMapper.findById(user.getId());
        if (dbUser == null) {
            return false;
        }
        return dbUser.getPassword().equals(user.getPassword());
    }

    private UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
} 