package com.fifo.ticketing.domain.like.service

import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.like.dto.LikeRequest
import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.like.repository.LikeRepository
import com.fifo.ticketing.domain.performance.entity.*
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.seat.entity.*
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.entity.File
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class LikeServiceTest {

    private val likeRepository = mockk<LikeRepository>()
    private val likeCountRepository = mockk<LikeCountRepository>()
    private val userRepository = mockk<UserRepository>()
    private val performanceRepository = mockk<PerformanceRepository>()

    private lateinit var likeService: LikeService

    private val userId = 1L
    private val performanceId = 100L

    private lateinit var mockUser: User
    private lateinit var place: Place
    private lateinit var mockFile: File
    private lateinit var mockPerformance: Performance
    private lateinit var mockLikeCount: LikeCount

    @BeforeEach
    fun setUp() {
        likeService = LikeService(
            likeRepository, likeCountRepository, userRepository, performanceRepository
        )

        mockUser = User.builder()
            .id(userId)
            .email("test@fifo.com")
            .username("테스트 유저")
            .password("secure")
            .build()

        place = Place.builder()
            .id(1L)
            .name("강남아트홀")
            .address("서울시 강남구")
            .totalSeats(100)
            .build()

        mockFile = File.builder()
            .id(1L)
            .encodedFileName("poster.png")
            .originalFileName("poster-original.png")
            .build()

        mockPerformance = Performance.builder()
            .id(performanceId)
            .title("오페라의 유령")
            .description("명작 뮤지컬")
            .startTime(LocalDateTime.of(2025, 10, 5, 19, 30))
            .endTime(LocalDateTime.of(2025, 10, 5, 22, 0))
            .reservationStartTime(LocalDateTime.of(2025, 9, 1, 12, 0))
            .place(place)
            .category(Category.MOVIE)
            .file(mockFile)
            .performanceStatus(false)
            .deletedFlag(false)
            .build()

        mockLikeCount = LikeCount.builder()
            .id(1L)
            .performance(mockPerformance)
            .likeCount(1)
            .build()
    }

    @Test
    fun `처음 좋아요 누르면 저장되고 likeCount 증가`() {
        val request = LikeRequest(performanceId)

        every { userRepository.findById(userId) } returns Optional.of(mockUser)
        every { performanceRepository.findById(performanceId) } returns Optional.of(mockPerformance)
        every { likeRepository.findByUserAndPerformance(mockUser, mockPerformance) } returns null
        every { likeCountRepository.findByPerformance(mockPerformance) } returns mockLikeCount
        every { likeRepository.save(any()) } returns mockk()
        every { likeCountRepository.save(any()) } returns mockLikeCount

        val result = likeService.toggleLike(userId, request)

        assertThat(result).isTrue()
        assertThat(mockLikeCount.likeCount).isEqualTo(2L)

        verify { likeRepository.save(any()) }
        verify { likeCountRepository.save(mockLikeCount) }
    }

    @Test
    fun `이미 좋아요 누른 상태면 취소되고 likeCount 감소`() {
        val request = LikeRequest(performanceId)

        val existingLike = Like.builder()
            .user(mockUser)
            .performance(mockPerformance)
            .isLiked(true)
            .build()

        every { userRepository.findById(userId) } returns Optional.of(mockUser)
        every { performanceRepository.findById(performanceId) } returns Optional.of(mockPerformance)
        every { likeRepository.findByUserAndPerformance(mockUser, mockPerformance) } returns existingLike
        every { likeCountRepository.findByPerformance(mockPerformance) } returns mockLikeCount
        every { likeRepository.save(any()) } returns existingLike
        every { likeCountRepository.save(any()) } returns mockLikeCount

        val result = likeService.toggleLike(userId, request)

        assertThat(result).isFalse()
        assertThat(existingLike.getIsLiked()).isFalse()
        assertThat(mockLikeCount.likeCount).isEqualTo(0L)
    }

    @Test
    fun `좋아요 취소한 상태에서 다시 누르면 likeCount 증가`() {
        val request = LikeRequest(performanceId)

        val existingLike = Like.builder()
            .user(mockUser)
            .performance(mockPerformance)
            .isLiked(false)
            .build()

        every { userRepository.findById(userId) } returns Optional.of(mockUser)
        every { performanceRepository.findById(performanceId) } returns Optional.of(mockPerformance)
        every { likeRepository.findByUserAndPerformance(mockUser, mockPerformance) } returns existingLike
        every { likeCountRepository.findByPerformance(mockPerformance) } returns mockLikeCount
        every { likeRepository.save(any()) } returns existingLike
        every { likeCountRepository.save(any()) } returns mockLikeCount

        val result = likeService.toggleLike(userId, request)

        assertThat(result).isTrue()
        assertThat(existingLike.getIsLiked()).isTrue()
        assertThat(mockLikeCount.likeCount).isEqualTo(2L)
    }
}