package com.nchu.library.controller;

import com.nchu.library.entity.BorrowRecord;
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

    // 获取当前登录用户ID的辅助方法
    private Long getCurrentUserId(Authentication authentication) {
        // Authentication 中的 principal 即学号，我们需要根据学号查用户ID
        // 这里简化：直接在 controller 中使用 UserRepository（为避免改动，我们使用 userService 或注入 repository）
        // 为保持代码整洁，我们在 controller 注入 UserRepository（生产环境建议用 Service 封装）
        return null; // 将在下面实际方法中实现
    }

    // 更好的做法：注入 UserRepository 并提取用户 ID
    private final com.nchu.library.repository.UserRepository userRepository;

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("未登录");
        }
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

    // 借阅历史
    @GetMapping("/history")
    public ResponseEntity<List<BorrowRecord>> getHistory(Authentication authentication) {
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(borrowService.getUserHistory(userId));
    }
}