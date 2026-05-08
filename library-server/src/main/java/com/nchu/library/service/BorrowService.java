package com.nchu.library.service;

import com.nchu.library.entity.*;
import com.nchu.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;

    private static final int MAX_BORROW_COUNT = 5; // 最大借阅数限制
    private static final int DEFAULT_BORROW_DAYS = 30; // 默认借阅天数

    /**
     * 借阅图书：优化校验逻辑
     */
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        // 1. 用户基本校验
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("借阅失败：用户不存在"));

        // 2. 借阅证状态校验
        if (!"正常".equals(user.getCardStatus())) {
            throw new RuntimeException("借阅失败：借阅证处于" + user.getCardStatus() + "状态，无法借阅");
        }

        // 3. 获取用户所有记录用于多重校验
        List<BorrowRecord> userRecords = borrowRecordRepository.findByUserId(userId);

        // 4. 逾期校验：如果有书逾期，禁止借新书
        boolean hasOverdue = userRecords.stream().anyMatch(r -> "逾期".equals(r.getStatus()));
        if (hasOverdue) {
            throw new RuntimeException("借阅失败：您有逾期未归还的图书，请先办理归还手续");
        }

        // 5. 借阅数量上限校验
        long activeCount = userRecords.stream()
                .filter(r -> "借阅中".equals(r.getStatus()))
                .count();
        if (activeCount >= MAX_BORROW_COUNT) {
            throw new RuntimeException("借阅失败：每人最多只能同时借阅 " + MAX_BORROW_COUNT + " 本图书");
        }

        // 6. 图书库存与重复借阅校验
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("借阅失败：图书不存在"));

        if (book.getAvailable() <= 0) {
            throw new RuntimeException("借阅失败：图书《" + book.getTitle() + "》已全部借出，请尝试预约");
        }

        boolean alreadyBorrowed = userRecords.stream()
                .anyMatch(r -> r.getBook().getId().equals(bookId) && "借阅中".equals(r.getStatus()));
        if (alreadyBorrowed) {
            throw new RuntimeException("借阅失败：您已持有该书且尚未归还");
        }

        // 7. 执行借阅：更新库存并保存记录
        book.setAvailable(book.getAvailable() - 1);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(DEFAULT_BORROW_DAYS));
        record.setStatus("借阅中");
        return borrowRecordRepository.save(record);
    }

    /**
     * 续借：增加有效期判断
     */
    @Transactional
    public BorrowRecord renewBook(Long recordId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("续借失败：借阅记录不存在"));

        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("续借失败：无权操作他人借阅记录");
        }

        if (!"借阅中".equals(record.getStatus())) {
            throw new RuntimeException("续借失败：图书当前状态为" + record.getStatus() + "，无法办理续借");
        }

        // 如果已经逾期，不能直接续借，必须先归还
        if (record.getDueDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("续借失败：图书已逾期，请前往图书馆柜台归还");
        }

        // 延长30天
        record.setDueDate(record.getDueDate().plusDays(DEFAULT_BORROW_DAYS));
        return borrowRecordRepository.save(record);
    }

    /**
     * 归还：优化预约通知逻辑
     */
    @Transactional
    public BorrowRecord returnBook(Long recordId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("归还失败：借阅记录不存在"));

        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("归还失败：无权操作他人记录");
        }

        if ("已归还".equals(record.getStatus())) {
            throw new RuntimeException("操作重复：该书已处于归还状态");
        }

        // 1. 更新归还记录
        record.setReturnDate(LocalDateTime.now());
        record.setStatus("已归还");
        borrowRecordRepository.save(record);

        // 2. 恢复库存
        Book book = record.getBook();
        book.setAvailable(book.getAvailable() + 1);
        bookRepository.save(book);

        // 3. 预约处理：仅通知最早的一位预约者（FIFO）
        List<Reservation> reservations = reservationRepository.findByBookIdAndStatus(book.getId(), "预约中");
        reservations.stream()
                .min(Comparator.comparing(Reservation::getReserveDate))
                .ifPresent(oldestRes -> {
                    oldestRes.setStatus("到书通知");
                    oldestRes.setNotifyDate(LocalDateTime.now());
                    reservationRepository.save(oldestRes);

                    // 发送针对性的到书消息通知
                    Notification notification = new Notification();
                    notification.setUser(oldestRes.getUser());
                    notification.setContent("到书通知：您预约的图书《" + book.getTitle() + "》已到馆，请于3日内前往图书馆借阅。");
                    notificationRepository.save(notification);
                });

        return record;
    }

    /**
     * 获取借阅历史：按日期倒序排列
     */
    public List<BorrowRecord> getUserHistory(Long userId) {
        List<BorrowRecord> history = borrowRecordRepository.findByUserId(userId);

        return history.stream()
                .sorted((a, b) -> {
                    // 修复点：手动处理可能存在的 null 日期，防止 400 错误
                    if (a.getBorrowDate() == null) return 1;
                    if (b.getBorrowDate() == null) return -1;
                    return b.getBorrowDate().compareTo(a.getBorrowDate()); // 倒序排列
                })
                .collect(Collectors.toList());
    }

    /**
     * 定时任务：逾期检测与通知
     */
    @Transactional
    public void checkOverdueAndNotify() {
        List<BorrowRecord> activeRecords = borrowRecordRepository.findAll().stream()
                .filter(r -> "借阅中".equals(r.getStatus()))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();
        for (BorrowRecord record : activeRecords) {
            if (record.getDueDate().isBefore(now)) {
                // 标记为逾期
                record.setStatus("逾期");
                borrowRecordRepository.save(record);

                // 发送逾期提醒
                Notification notification = new Notification();
                notification.setUser(record.getUser());
                notification.setContent("逾期提醒：您借阅的《" + record.getBook().getTitle() + "》已逾期，请尽快归还以免产生违约信用影响。");
                notificationRepository.save(notification);
            }
        }
    }
}