package com.nchu.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // 必须引入
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
    @JsonIgnore // 关键修改：防止序列化 User 时产生循环引用导致事务回滚
    private User user;

    @ManyToOne(fetch = FetchType.EAGER) // 立即加载图书信息，确保前端能直接渲染书名
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private LocalDateTime borrowDate = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

    @Column(nullable = false)
    private String status = "借阅中";
}