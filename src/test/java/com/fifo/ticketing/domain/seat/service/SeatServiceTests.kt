package com.fifo.ticketing.domain.seat.service

import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.AlertDetailException
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class SeatServiceTests {

    val seatRepository = mockk<SeatRepository>()

    val entityManager = mockk<EntityManager>()

    val seatService = SeatService(
        seatRepository, entityManager
    )

    private lateinit var mockplace: Place
    private lateinit var mockFile: File
    private lateinit var mockPerformance: Performance
    private lateinit var mockGrade: Grade
    private lateinit var mockSeat: Seat

    @BeforeEach
    fun setUp() {
        mockplace = Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100)

        mockFile = File(1L, "poster.jpg", "sample.jpg")

        mockPerformance = Performance(
            1L, "라따뚜이", "라따뚜이는 픽시의 영화입니다.", mockplace,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            Category.MOVIE,
            false,
            false,
            LocalDateTime.of(2025, 5, 12, 19, 0),
            mockFile
        )

        mockGrade = Grade(1L, mockplace, "A", 5000, 10)

        mockSeat = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.AVAILABLE)
    }

    @Test
    fun `validateBookSeats - 좌석이 AVAILABLE 상태면 BOOKED 상태로 바꿔서 반환한다`() {
        val seatIds = listOf(1L, 2L, 3L)

        val mockSeat1 = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.AVAILABLE)
        val mockSeat2 = Seat(2L, mockPerformance, "A2", 5000, mockGrade, SeatStatus.AVAILABLE)
        val mockSeat3 = Seat(3L, mockPerformance, "A3", 5000, mockGrade, SeatStatus.AVAILABLE)

        every { seatRepository.findAllByIdInWithOptimisticLock(seatIds) } returns listOf( mockSeat1, mockSeat2, mockSeat3 )
        every { seatRepository.flush() } just Runs


        val result = seatService.validateBookSeats(seatIds)

        3 shouldBe result.size
        SeatStatus.BOOKED shouldBe result[0].seatStatus
        SeatStatus.BOOKED shouldBe result[1].seatStatus
        SeatStatus.BOOKED shouldBe result[2].seatStatus
    }

    @Test
    fun `validateBookSeats - 좌석이 이미 BOOKED 상태면 AlertDetailException이 터진다`() {
        val seatIds = listOf(1L, 2L)

        val mockSeat1 = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.BOOKED)
        val mockSeat2 = Seat(2L, mockPerformance, "A2", 5000, mockGrade, SeatStatus.BOOKED)

        every { seatRepository.findAllByIdInWithOptimisticLock(seatIds) } returns listOf( mockSeat1, mockSeat2 )
        every { seatRepository.flush() } just Runs

        val exception = assertThrows<AlertDetailException> {
            seatService.validateBookSeats(seatIds)
        }

        exception.errorCode shouldBe ErrorCode.SEAT_ALREADY_BOOKED

    }

    @Test
    fun `changeSeatStatus - 입력 값에 따라 좌석 상태가 변경된다`() {
        val mockBook = mockk<Book>()
        val bookSeat = BookSeat(1, mockBook, mockSeat)
        val bookSeatList = listOf(bookSeat)

        seatService.changeSeatStatus(bookSeatList, SeatStatus.OCCUPIED)

        mockSeat.seatStatus shouldBe SeatStatus.OCCUPIED
    }

    @Test
    fun `changeSeatStatus - 존재하지 않는 좌석 상태입려되면 예외가 발생한다`() {
        val mockBook = mockk<Book>()
        val bookSeat = BookSeat(1, mockBook, mockSeat)
        val bookSeatList = listOf(bookSeat)

        val exception = shouldThrow<ErrorException> {
            seatService.changeSeatStatus(
                bookSeatList,
                SeatStatus.DELETED
            )
        }

        exception.errorCode shouldBe ErrorCode.NOT_FOUND_SEAT_STATUS
    }

    @Test
    fun `getSeatsForPerformance - 공연 ID에 해당하는 좌석을 찾고 DTO로 반환한다`() {
        val performanceId = 1L

        every { seatRepository.findValidSeatsByPerformanceId(performanceId) } returns listOf(
            mockSeat
        )

        val result = seatService.getSeatsForPerformance(performanceId)

        result.size shouldBe 1
        result[0].seatId shouldBe 1
    }

    @Test
    fun `createSeats - 좌석 리스트를 EntityManager를 통해 배치 저장한다`() {
        val seatList = (1..5).map {
            Seat(it.toLong(), mockPerformance, "A$it", 5000, mockGrade, SeatStatus.AVAILABLE)
        }

        every { entityManager.persist(any<Seat>()) } just Runs
        every { entityManager.flush() } just Runs
        every { entityManager.clear() } just Runs

        seatService.createSeats(seatList)

        seatList.forEach { seat ->
            verify { entityManager.persist(seat) }
        }

        verify(exactly = 1) { entityManager.flush() }
        verify(exactly = 1) { entityManager.clear() }
    }

}