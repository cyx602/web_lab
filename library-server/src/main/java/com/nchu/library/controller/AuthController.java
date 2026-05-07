package com.nchu.library.controller;

import com.nchu.library.dto.LoginRequest;
import com.nchu.library.dto.LoginResponse;
import com.nchu.library.dto.RegisterRequest; // 新增
import com.nchu.library.dto.UpdatePasswordRequest; // 新增
import com.nchu.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 新增
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    // 新增：注册接口
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    // 新增：修改密码接口 (需要登录后操作)
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication) {
        // 从 Token 中获取当前登录用户的学号
        String studentId = (String) authentication.getPrincipal();
        authService.updatePassword(studentId, request);
        return ResponseEntity.ok("密码修改成功");
    }
}