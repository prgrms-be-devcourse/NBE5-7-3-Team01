package com.fifo.ticketing.global.scheduler

import com.fifo.ticketing.domain.like.service.LikeMailNotificationService
import com.fifo.ticketing.domain.performance.service.PerformanceReservationSystemService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class SystemScheduler(
    private val likeMailNotificationService: LikeMailNotificationService,
    private val performanceReservationSystemService: PerformanceReservationSystemService
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
        performanceReservationSystemService.updateStatusIfReservationStart()
    }

    @Scheduled(cron = "0 */10 * * * *")
    fun updatePerformanceStatusIfSoldOutOrCanceled() {
        performanceReservationSystemService.updateStatusIfSoldOutOrCanceled()
    }
}