package com.nchu.library.service;

import com.nchu.library.dto.LoginRequest;
import com.nchu.library.dto.LoginResponse;
import com.nchu.library.entity.User;
import com.nchu.library.repository.UserRepository;
import com.nchu.library.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
}