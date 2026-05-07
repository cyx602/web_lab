package com.nchu.library.controller;

import com.nchu.library.entity.Review;
import com.nchu.library.service.ReviewService;
import com.nchu.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    private Long extractUserId(Authentication authentication) {
        String studentId = (String) authentication.getPrincipal();
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("用户不存在")).getId();
    }

    // 发表评价
    @PostMapping("/{bookId}")
    public ResponseEntity<Review> postReview(@PathVariable Long bookId,
                                             @RequestBody Map<String, Object> payload,
                                             Authentication authentication) {
        Long userId = extractUserId(authentication);
        Integer rating = (Integer) payload.get("rating");
        String content = (String) payload.get("content");
        return ResponseEntity.ok(reviewService.addReview(userId, bookId, rating, content));
    }

    // 获取某本书的所有评价（无需登录也可查看）
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getBookReviews(bookId));
    }
}