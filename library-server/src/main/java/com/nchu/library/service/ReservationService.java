package com.nchu.library.service;

import com.nchu.library.entity.*;
import com.nchu.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReservationRepository reservationRepository;

    // 预约图书
    @Transactional
    public Reservation reserveBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("图书不存在"));

        // 如果图书可借，直接提示去借阅（这里简化：只有已全部借出才允许预约）
        if (book.getAvailable() > 0) {
            throw new RuntimeException("该书仍有库存，可直接借阅，无需预约");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReserveDate(LocalDateTime.now());
        reservation.setStatus("预约中");
        return reservationRepository.save(reservation);
    }

    // 获取当前用户的预约列表
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }
}