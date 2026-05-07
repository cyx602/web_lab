package com.nchu.library.repository;

import com.nchu.library.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 获取某本书的所有评价，并按时间倒序排列
    List<Review> findByBookIdOrderByCreateTimeDesc(Long bookId);
}