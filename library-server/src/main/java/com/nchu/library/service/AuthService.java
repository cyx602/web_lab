package com.nchu.library.service;

import com.nchu.library.dto.LoginRequest;
import com.nchu.library.dto.LoginResponse;
import com.nchu.library.dto.RegisterRequest; // 新增
import com.nchu.library.dto.UpdatePasswordRequest; // 新增
import com.nchu.library.entity.User;
import com.nchu.library.repository.UserRepository;
import com.nchu.library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 1. 登录逻辑 (保持不变)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByStudentId(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getStudentId());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("studentId", user.getStudentId());
        userInfo.put("name", user.getName());
        userInfo.put("cardStatus", user.getCardStatus());

        return new LoginResponse(token, userInfo);
    }

    // 2. 新增：注册逻辑
    @Transactional
    public String register(RegisterRequest request) {
        // 检查学号是否已存在
        if (userRepository.findByStudentId(request.getStudentId()).isPresent()) {
            throw new RuntimeException("该学号已被注册");
        }

        User user = new User();
        user.setStudentId(request.getStudentId());
        // 注意：SecurityConfig 目前使用 NoOpPasswordEncoder，所以这里存的是明文
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setCardStatus("正常");

        userRepository.save(user);
        return "注册成功";
    }

    // 3. 新增：修改密码逻辑
    @Transactional
    public void updatePassword(String studentId, UpdatePasswordRequest request) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 校验旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}