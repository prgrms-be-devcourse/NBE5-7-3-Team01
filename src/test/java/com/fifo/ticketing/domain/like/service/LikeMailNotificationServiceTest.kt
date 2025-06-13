package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.domain.user.entity.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class LikeMailNotificationServiceTest {

    private val likeRepository = mockk<LikeRepository>()
    private val bookRepository = mockk<BookRepository>()
    private val seatRepository = mockk<SeatRepository>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private lateinit var service: LikeMailNotificationService
    private lateinit var user: User
    private lateinit var performance: Performance
    private lateinit var like: Like

    @BeforeEach
    fun setUp() {
        user = User.builder()
            .id(1L)
            .email("test@example.com")
            .username("홍길동")
            .provider("google")
            .build()

        val place = Place(1L, "서울시 강남구", "강남아트홀", 100)

        performance = Performance(
            1L, "공연", "설명", place,
            LocalDateTime.now().plusMinutes(30),
            LocalDateTime.now().plusHours(2),
            Category.CONCERT, false, false,
            LocalDateTime.now().minusDays(1)
        )

        like = Like.builder()
            .id(1L)
            .user(user)
            .performance(performance)
            .isLiked(true)
            .build()

        service = LikeMailNotificationService(
            likeRepository, eventPublisher, bookRepository, seatRepository
        )
    }

    @Test
    fun `공연 30분 전 알림 이벤트 발송`() {
        every { likeRepository.findLikesByTargetTime(any(), any()) } returns listOf(like)

        service.sendTimeNotification()

        verify { eventPublisher.publishEvent(any<ReservationStartMailDto>()) }
    }

    @Test
    fun `결제하지 않은 유저 알림 발송`() {
        every { likeRepository.findLikesByTargetTime(any(), any()) } returns listOf(like)
        every {
            bookRepository.existsByUserAndPerformanceAndBookStatus(
                user,
                performance,
                BookStatus.PAYED
            )
        } returns false
        every { seatRepository.countAvailableSeatsByPerformanceId(performance.id!!) } returns 10

        service.sendNoPayedNotification()

        verify { eventPublisher.publishEvent(any<NoPayedMailDto>()) }
    }

    @Test
    fun `결제한 유저는 알림 발송 안 함`() {
        every { likeRepository.findLikesByTargetTime(any(), any()) } returns listOf(like)
        every {
            bookRepository.existsByUserAndPerformanceAndBookStatus(
                user,
                performance,
                BookStatus.PAYED
            )
        } returns true

        service.sendNoPayedNotification()

        verify(inverse = true) { eventPublisher.publishEvent(any<NoPayedMailDto>()) }
    }
}