package com.nchu.library.service;

import com.nchu.library.entity.*;
import com.nchu.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationRepository notificationRepository;

    // 借阅图书：检查用户状态、库存、是否已有未还记录
    @Transactional
    public BorrowRecord borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!"正常".equals(user.getCardStatus())) {
            throw new RuntimeException("借阅证状态异常，无法借阅");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));
        if (book.getAvailable() <= 0) {
            throw new RuntimeException("库存不足，无法借阅");
        }

        // 检查是否已有未归还的同一图书
        List<BorrowRecord> unreturned = borrowRecordRepository.findByBookIdAndStatus(bookId, "借阅中");
        Optional<BorrowRecord> alreadyBorrowed = unreturned.stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .findAny();
        if (alreadyBorrowed.isPresent()) {
            throw new RuntimeException("您已借阅过该图书且尚未归还");
        }

        book.setAvailable(book.getAvailable() - 1);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord();
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30)); // 默认借阅30天
        record.setStatus("借阅中");
        return borrowRecordRepository.save(record);
    }

    // 续借：延长借阅期限（例如再延长30天）
    @Transactional
    public BorrowRecord renewBook(Long recordId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"借阅中".equals(record.getStatus())) {
            throw new RuntimeException("当前状态不允许续借");
        }
        record.setDueDate(record.getDueDate().plusDays(30));
        return borrowRecordRepository.save(record);
    }

    // 归还图书
    @Transactional
    public BorrowRecord returnBook(Long recordId, Long userId) {
        BorrowRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("借阅记录不存在"));
        if (!record.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        if (!"借阅中".equals(record.getStatus())) {
            throw new RuntimeException("该书已归还或状态异常");
        }

        record.setReturnDate(LocalDateTime.now());
        record.setStatus("已归还");
        borrowRecordRepository.save(record);

        // 恢复库存
        Book book = record.getBook();
        book.setAvailable(book.getAvailable() + 1);
        bookRepository.save(book);

        // 检查是否有该书的预约，生成到书通知
        List<Reservation> reservations = reservationRepository.findByBookIdAndStatus(book.getId(), "预约中");
        for (Reservation res : reservations) {
            res.setStatus("到书通知");
            res.setNotifyDate(LocalDateTime.now());
            reservationRepository.save(res);

            // 发送通知
            Notification notification = new Notification();
            notification.setUser(res.getUser());
            notification.setContent("您预约的《" + book.getTitle() + "》已到书，请尽快来馆借阅。");
            notificationRepository.save(notification);
        }

        return record;
    }

    // 获取当前用户所有借阅记录
    public List<BorrowRecord> getUserHistory(Long userId) {
        return borrowRecordRepository.findByUserId(userId);
    }

    // 检测逾期并给逾期记录的用户发通知（实际可由定时任务调用）
    @Transactional
    public void checkOverdueAndNotify() {
        List<BorrowRecord> all = borrowRecordRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (BorrowRecord record : all) {
            if ("借阅中".equals(record.getStatus()) && record.getDueDate().isBefore(now)) {
                // 标记为逾期
                record.setStatus("逾期");
                borrowRecordRepository.save(record);

                // 检查今天是否已经发送过逾期提醒（简单处理）
                List<Notification> recentNotifications = notificationRepository.findByUserIdAndIsReadFalse(record.getUser().getId());
                boolean alreadyNotified = recentNotifications.stream()
                        .anyMatch(n -> n.getContent().contains("逾期") && n.getContent().contains(record.getBook().getTitle()));
                if (!alreadyNotified) {
                    Notification notification = new Notification();
                    notification.setUser(record.getUser());
                    notification.setContent("您借阅的《" + record.getBook().getTitle() + "》已逾期，请尽快归还。");
                    notificationRepository.save(notification);
                }
            }
        }
    }
}