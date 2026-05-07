package com.nchu.library.task;

import com.nchu.library.service.BorrowService;
import com.nchu.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueTask {

    private final BorrowService borrowService;
    private final ReservationService reservationService;

    // 每天凌晨 1 点执行一次检查
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkOverdueBooks() {
        log.info("开始执行自动逾期检测任务...");
        borrowService.checkOverdueAndNotify();
        log.info("逾期检测任务执行完毕。");
    }

    // 在类中增加定时执行逻辑
    @Scheduled(cron = "0 30 1 * * ?") // 每天凌晨 1:30 执行
    public void autoCancelReservations() {
        log.info("开始执行自动清理过期预约任务...");
        reservationService.cleanupExpiredReservations();
        log.info("过期预约清理完毕。");
    }

}