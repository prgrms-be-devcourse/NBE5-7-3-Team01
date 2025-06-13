package com.fifo.ticketing.global.scheduler

import com.fifo.ticketing.domain.like.service.LikeMailNotificationService
import com.fifo.ticketing.domain.performance.service.PerformanceReservationOpenService
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component

class NotificationScheduler(
    private val likeMailNotificationService: LikeMailNotificationService,
    private val performanceReservationOpenService: PerformanceReservationOpenService
) {
    @Scheduled(cron = "0 30 12 * * *")
    fun likeMailNotification() {
        likeMailNotificationService.sendTimeNotification()
    }

    @Scheduled(cron = "0 0 2 * * *")
    fun noPayedNotification() {
        likeMailNotificationService.sendNoPayedNotification()
    }

    @Scheduled(cron = "0 0 13 * * *")
    fun updatePerformanceStatus() {
        performanceReservationOpenService.updateStatusIfReservationStart()
    }
}
