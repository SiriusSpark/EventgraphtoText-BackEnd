package com.example.myapp.controller;

import com.example.myapp.dto.*;
import com.example.myapp.entity.User;
import com.example.myapp.service.UserService;
import com.example.myapp.util.JwtUtil;
import com.example.myapp.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@Tag(name = "用户管理", description = "用户相关的所有操作，包括注册、登录、信息管理等")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final String uploadDir = "uploads/avatars/";

    public UserController(UserService userService, UserMapper userMapper, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        // 确保上传目录存在
        new File(uploadDir).mkdirs();
    }

    @Operation(summary = "用户注册", description = "新用户注册，创建账号")
    @PostMapping("/register")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Map<String, Object>>> register(
            @RequestBody UserRegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setNickname(request.getNickname());
            user.setPassword(request.getPassword());
            user.setEmail(request.getEmail());
            user.setAvatarUrl(request.getAvatarUrl());

            UserDTO createdUser = userService.createUser(user);
            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("注册成功",
                    Map.of("user_id", createdUser.getId())));
        } catch (RuntimeException e) {
            // 处理已知的业务异常
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            // 处理其他未知异常
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("注册失败，请稍后重试"));
        }
    }

    @Operation(summary = "用户登录", description = "用户登录并获取认证令牌")
    @PostMapping("/login")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Map<String, Object>>> login(
            @RequestBody UserLoginRequest request) {
        try {
            System.out.println("开始登录处理，用户名: " + request.getUsername());

            // 1. 根据用户名查找用户
            User user = userMapper.findByUsername(request.getUsername());
            if (user == null) {
                System.out.println("用户不存在: " + request.getUsername());
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("用户名或密码错误"));
            }
            System.out.println("找到用户: " + user.getUsername() + ", ID: " + user.getId());

            // 2. 验证密码
            System.out.println("开始验证密码");
            System.out.println("数据库中的密码: " + user.getPassword());
            System.out.println("用户输入的密码: " + request.getPassword());
            if (!user.getPassword().equals(request.getPassword())) {
                System.out.println("密码不匹配");
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("用户名或密码错误"));
            }
            System.out.println("密码验证成功");

            // 3. 生成token
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            System.out.println("生成的token: " + token);

            // 4. 更新最后活动时间
            user.setLastActiveAt(LocalDateTime.now());
            int updateResult = userMapper.update(user);
            System.out.println("更新用户活动时间结果: " + updateResult);

            // 5. 返回用户信息和token
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("nickname", user.getNickname() != null ? user.getNickname() : "");
            userMap.put("email", user.getEmail() != null ? user.getEmail() : "");
            userMap.put("avatar_url", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
            System.out.println("返回的用户信息: " + userMap);

            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("access_token", token);
            tokenMap.put("token_type", "Bearer");

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", tokenMap);
            responseData.put("user", userMap);

            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("登录成功", responseData));
        } catch (Exception e) {
            System.out.println("登录过程中发生异常:");
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(com.example.myapp.common.ApiResponse.error("登录失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public ResponseEntity<com.example.myapp.common.ApiResponse<UserDTO>> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserDTO user = userService.getUserById(userId);
        // 确保email字段不为null
        if (user.getEmail() == null) {
            user.setEmail("");
        }
        if (user.getNickname() == null) {
            user.setNickname("");
        }
        if (user.getAvatarUrl() == null) {
            user.setAvatarUrl("");
        }
        return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("获取成功", user));
    }

    @Operation(summary = "更新用户信息", description = "更新指定用户的基本信息")
    @PutMapping("/{id}")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Void>> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody User user) {
        try {
            user.setId(id);
            userService.updateUser(user);
            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("用户信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("用户不存在"));
        }
    }

    @Operation(summary = "修改密码", description = "修改指定用户的登录密码")
    @PutMapping("/{id}/password")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Void>> updatePassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody PasswordUpdateRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute("userId");
            if (!currentUserId.equals(id)) {
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("无权修改其他用户的密码"));
            }

            boolean success = userService.updatePassword(id, request.getOldPassword(), request.getNewPassword());
            if (success) {
                return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("密码更新成功"));
            } else {
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("原密码错误"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("密码更新失败"));
        }
    }

    @Operation(summary = "更新活动时间", description = "更新用户的最后活动时间")
    @PutMapping("/{id}/last-active")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Void>> updateLastActive(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute("userId");
            if (!currentUserId.equals(id)) {
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("无权更新其他用户的活动时间"));
            }

            User user = new User();
            user.setId(id);
            user.setLastActiveAt(LocalDateTime.parse(request.get("last_active_at")));
            userService.updateUser(user);
            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("最后活动时间更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("请求无效"));
        }
    }

    @Operation(summary = "删除用户", description = "删除指定的用户账号")
    @DeleteMapping("/{id}")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Void>> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("用户不存在"));
        }
    }

    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "获取头像", description = "获取用户的头像图片")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Map<String, String>>> getAvatar(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            User user = userMapper.findById(id);
            if (user == null || user.getAvatarUrl() == null) {
                return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("获取成功",
                        Map.of("avatar_url", "https://example.com/avatars/default.png")));
            }
            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("获取成功",
                    Map.of("avatar_url", user.getAvatarUrl())));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("获取头像失败"));
        }
    }

    @Operation(summary = "更新头像", description = "更新用户的头像图片")
    @PutMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.example.myapp.common.ApiResponse<Map<String, String>>> updateAvatar(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) {
        try {
            Long currentUserId = (Long) httpRequest.getAttribute("userId");
            if (!currentUserId.equals(id)) {
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("无权更新其他用户的头像"));
            }

            // 计算文件的MD5哈希值
            byte[] bytes = file.getBytes();
            String fileHash = org.springframework.util.DigestUtils.md5DigestAsHex(bytes);
            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = fileHash + extension;
            Path filePath = Paths.get(uploadDir + fileName);

            // 检查文件是否已存在
            if (!Files.exists(filePath)) {
                // 确保目录存在
                Files.createDirectories(filePath.getParent());
                // 保存文件
                Files.write(filePath, bytes);
                System.out.println("新文件已保存: " + fileName);
            } else {
                System.out.println("文件已存在，直接使用: " + fileName);
            }

            String avatarUrl = "/uploads/avatars/" + fileName;

            // 获取现有用户信息
            User existingUser = userMapper.findById(id);
            if (existingUser == null) {
                return ResponseEntity.badRequest().body(com.example.myapp.common.ApiResponse.error("用户不存在"));
            }

            // 只更新头像URL
            existingUser.setAvatarUrl(avatarUrl);
            userService.updateUser(existingUser);

            return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("头像更新成功",
                    Map.of("avatar_url", avatarUrl)));
        } catch (Exception e) {
            System.out.println("更新头像时发生错误: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(com.example.myapp.common.ApiResponse.error("头像更新失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "退出登录", description = "用户退出登录，注销认证令牌")
    @PostMapping("/logout")
    public ResponseEntity<com.example.myapp.common.ApiResponse<Void>> logout(
            @Parameter(description = "认证令牌") @RequestHeader("Authorization") String token) {
        // JWT是无状态的，客户端只需要删除token即可
        return ResponseEntity.ok(com.example.myapp.common.ApiResponse.success("退出登录成功"));
    }

    private String getFileExtension(String fileName) {
        if (fileName == null)
            return ".jpg";
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        // 只允许特定的图片格式
        if (!Arrays.asList(".jpg", ".jpeg", ".png", ".gif").contains(extension)) {
            return ".jpg";
        }
        return extension;
    }
}