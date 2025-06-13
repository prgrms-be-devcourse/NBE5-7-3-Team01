package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.like.mapper.LikeMailMapper.toNoPayedMailDto
import com.fifo.ticketing.domain.like.mapper.LikeMailMapper.toReservationStartMailDto
import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.domain.user.entity.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.function.Consumer

@Service
class LikeMailNotificationService(
    private val likeRepository: LikeRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val bookRepository: BookRepository,
    private val seatRepository: SeatRepository
) {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun sendTimeNotification() {
        val now = LocalDateTime.now()
        val targetTime = now.plusMinutes(30)
        val start = targetTime.minusMinutes(30)
        val end = targetTime.plusMinutes(30)

        likeRepository.findLikesByTargetTime(start, end)
            .filter { isEmailSendTarget(it.getUser()) }
            .map { toReservationStartMailDto(it.getUser(), it.getPerformance()) }
            .forEach { dto -> eventPublisher.publishEvent(dto) }
    }

    @Transactional
    fun sendNoPayedNotification() {
        val now = LocalDateTime.now()
        val reservationTime = now.minusMinutes(60)
        val start = reservationTime.minusMinutes(30)
        val end = reservationTime.plusMinutes(30)

        likeRepository.findLikesByTargetTime(start, end)
            .forEach(Consumer { notifyNoPay(it) })
    }

    private fun notifyNoPay(like: Like) {
        val user = like.user
        val performance = like.performance
        val payed = bookRepository.existsByUserAndPerformanceAndBookStatus(user, performance, BookStatus.PAYED)

        if (!isEmailSendTarget(user) || payed) {
            return  //  메일 전송 조건 안되면 skip
        }

        val availableSeats = seatRepository.countAvailableSeatsByPerformanceId(performance.id!!)

        log.info { "예약 여부: $payed, 잔여좌석: $availableSeats" }

        val dto = toNoPayedMailDto(user, performance, availableSeats)
        eventPublisher.publishEvent(dto)
    }

    private fun isEmailSendTarget(user: User): Boolean {
        val provider = user.provider
        val email = user.email
        return (provider == null || provider.equals("google", ignoreCase = true)) && email != null
    }
}