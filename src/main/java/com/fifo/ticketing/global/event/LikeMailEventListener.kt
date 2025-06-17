package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.global.service.MailService
import org.hibernate.query.sqm.tree.SqmNode.log

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
        log.info("📨 [${Thread.currentThread().name}] 예약 시작 알림 메일 전송 시작 - ${dto.email}")
        mailService.sendReservationStartNoticeMail(dto)
    }

    @Async("mailExecutor")
    @EventListener
    fun handleLikeMailEvent(dto: NoPayedMailDto) {
        mailService.sendNoPayedNoticeMail(dto)
    }
}