package com.nchu.library.repository;

import com.nchu.library.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByBookIdAndStatus(Long bookId, String status);

    // 新增：查找状态为“到书通知”且通知时间早于某个时间点的记录
    List<Reservation> findByStatusAndNotifyDateBefore(String status, LocalDateTime dateTime);
}