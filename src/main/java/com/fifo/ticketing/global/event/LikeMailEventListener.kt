package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.global.service.MailService

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class LikeMailEventListener(
    private val mailService: MailService
) {

    @Async("mailExecutor")
    @EventListener
    fun handleLikeMailEvent(dto: ReservationStartMailDto) {
        mailService.sendReservationStartNoticeMail(dto)
    }

    @Async("mailExecutor")
    @EventListener
    fun handleLikeMailEvent(dto: NoPayedMailDto) {
        mailService.sendNoPayedNoticeMail(dto)
    }
}