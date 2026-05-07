package com.nchu.library.controller;

import com.nchu.library.entity.Reservation;
import com.nchu.library.repository.UserRepository;
import com.nchu.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    // 辅助方法：从 Token 提取用户 ID
    private Long extractUserId(Authentication authentication) {
        String studentId = (String) authentication.getPrincipal();
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();
    }

    // 1. 预约图书接口
    @PostMapping("/{bookId}")
    public ResponseEntity<Reservation> reserveBook(@PathVariable Long bookId, Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reservationService.reserveBook(userId, bookId));
    }

    // 2. 获取当前用户的预约列表接口
    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(reservationService.getUserReservations(userId));
    }
}