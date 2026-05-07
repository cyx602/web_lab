package com.nchu.library.controller;

import com.nchu.library.entity.User;
import com.nchu.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String studentId = (String) authentication.getPrincipal();
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    // 1. 获取个人详细信息
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        user.setPassword("******"); // 敏感信息脱敏
        return ResponseEntity.ok(user);
    }

    // 2. 修改个人基本信息 (如姓名、电话)
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User profileUpdate, Authentication authentication) {
        User user = getCurrentUser(authentication);
        user.setName(profileUpdate.getName());
        user.setPhone(profileUpdate.getPhone());
        return ResponseEntity.ok(userRepository.save(user));
    }

    // 3. 借阅证状态管理 (正常/挂失)
    @PutMapping("/card-status")
    public ResponseEntity<User> updateCardStatus(@RequestBody Map<String, String> payload, Authentication authentication) {
        User user = getCurrentUser(authentication);
        String status = payload.get("status");

        if (!"正常".equals(status) && !"挂失".equals(status)) {
            throw new RuntimeException("非法状态，仅支持'正常'或'挂失'");
        }

        user.setCardStatus(status);
        return ResponseEntity.ok(userRepository.save(user));
    }
}