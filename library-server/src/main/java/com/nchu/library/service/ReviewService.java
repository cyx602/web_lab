package com.nchu.library.service;

import com.nchu.library.entity.*;
import com.nchu.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public Review addReview(Long userId, Long bookId, Integer rating, String content) {
        // 增强逻辑：检查用户是否借阅并归还过此书
        List<BorrowRecord> records = borrowRecordRepository.findByUserId(userId);
        boolean hasReturned = records.stream()
                .anyMatch(r -> r.getBook().getId().equals(bookId) && "已归还".equals(r.getStatus()));

        if (!hasReturned) {
            throw new RuntimeException("只有归还图书后才能发表评价");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("图书不存在"));

        Review review = new Review();
        review.setUser(user);
        review.setBook(book);
        review.setRating(rating);
        review.setContent(content);
        return reviewRepository.save(review);
    }

    public List<Review> getBookReviews(Long bookId) {
        return reviewRepository.findByBookIdOrderByCreateTimeDesc(bookId);
    }
}