package com.nchu.library.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime borrowDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dueDate;     // 应还日期

    private LocalDateTime returnDate;  // 实际归还时间

    @Column(nullable = false)
    private String status = "借阅中";   // 借阅中/已归还/逾期
}