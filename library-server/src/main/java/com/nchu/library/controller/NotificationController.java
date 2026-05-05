package com.nchu.library.controller;

import com.nchu.library.entity.Notification;
import com.nchu.library.repository.NotificationRepository;
import com.nchu.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private Long extractUserId(Authentication authentication) {
        String studentId = (String) authentication.getPrincipal();
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();
    }

    // 获取当前用户的所有通知
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(notificationRepository.findByUserIdOrderByCreateTimeDesc(userId));
    }

    // 标记通知为已读
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok(notification);
    }
}