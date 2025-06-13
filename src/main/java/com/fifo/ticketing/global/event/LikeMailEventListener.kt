package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.like.service.LikeMailService

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class LikeMailEventListener (
    private val likeMailService: LikeMailService
){

    @Async("mailExecutor")
    @EventListener
    fun handleLikeMailEvent(event: ReservationEvent) {
        likeMailService.reservationStart(event.dto)
    }

    @Async("mailExecutor")
    @EventListener
    fun handleLikeMailEvent(event: MailEvent) {
        likeMailService.noPayedPerformance(event.dto)
    }
}