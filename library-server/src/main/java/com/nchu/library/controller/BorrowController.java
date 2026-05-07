package com.nchu.library.controller;

import com.nchu.library.entity.BorrowRecord;
import com.nchu.library.repository.UserRepository; // 引入 Repository
import com.nchu.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;
    private final UserRepository userRepository; // 注入 UserRepository

    // 统一的提取用户 ID 方法
    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
        // Principal 存储的是学号
        String studentId = (String) authentication.getPrincipal();
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在"))
                .getId();
    }

    // 借阅图书
    @PostMapping("/{bookId}")
    public ResponseEntity<BorrowRecord> borrowBook(@PathVariable Long bookId,
                                                   Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(borrowService.borrowBook(userId, bookId));
    }

    // 续借
    @PutMapping("/{recordId}/renew")
    public ResponseEntity<BorrowRecord> renewBook(@PathVariable Long recordId,
                                                  Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(borrowService.renewBook(recordId, userId));
    }

    // 归还
    @PutMapping("/{recordId}/return")
    public ResponseEntity<BorrowRecord> returnBook(@PathVariable Long recordId,
                                                   Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(borrowService.returnBook(recordId, userId));
    }

    // 获取当前登录用户的借阅历史
    @GetMapping("/history")
    public ResponseEntity<List<BorrowRecord>> getHistory(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(borrowService.getUserHistory(userId));
    }
}