package com.example.myapp.service;

import com.example.myapp.dto.UserDTO;
import com.example.myapp.entity.User;
import java.util.List;

public interface UserService {
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    List<UserDTO> getAllUsers();
    UserDTO createUser(User user);
    UserDTO updateUser(User user);
    void deleteUser(Long id);
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    boolean verifyPassword(User user);
} 