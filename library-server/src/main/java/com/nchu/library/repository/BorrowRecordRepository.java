package com.nchu.library.repository;

import com.nchu.library.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserId(Long userId);
    List<BorrowRecord> findByBookIdAndStatus(Long bookId, String status);
}