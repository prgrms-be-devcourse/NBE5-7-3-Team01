package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.entity.*
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.repository.BookScheduleRepository
import com.fifo.ticketing.domain.book.repository.BookSeatRepository
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.global.entity.File
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class BookScheduleManagerTests {

    val bookScheduleRepository = mockk<BookScheduleRepository>()
    val bookRepository = mockk<BookRepository>()
    val bookSeatRepository = mockk<BookSeatRepository>()
    val seatRepository = mockk<SeatRepository>()

    val dispatcher = StandardTestDispatcher()
    val coroutineScope = TestScope(dispatcher)

    val bookScheduleManager = spyk<BookScheduleManager>(
        objToCopy = BookScheduleManager(
            bookScheduleRepository,
            bookRepository,
            bookSeatRepository,
            seatRepository,
            coroutineScope
        ),

        )


    private lateinit var mockUser: User
    private lateinit var place: Place
    private lateinit var mockFile: File
    private lateinit var mockPerformance: Performance
    private lateinit var mockGrade: Grade
    private lateinit var mockSeat: Seat
    private lateinit var mockTask: BookScheduledTask

    @BeforeEach
    fun setUp() {

        mockUser = User.builder()
            .id(1L)
            .email("example@gmail.com")
            .password("123")
            .username("테스트 유저")
            .build()

        place = Place.builder()
            .id(1L)
            .address("서울특별시 서초구 서초동 1307")
            .name("강남아트홀")
            .totalSeats(100)
            .build()

        mockFile = File.builder()
            .id(1L)
            .encodedFileName("poster.jpg")
            .originalFileName("sample.jpg")
            .build()

        mockPerformance = Performance.builder()
            .id(1L)
            .title("라따뚜이")
            .description("라따뚜이는 픽사의 영화입니다.")
            .place(place)
            .startTime(LocalDateTime.of(2025, 6, 1, 19, 0))
            .endTime(LocalDateTime.of(2025, 6, 1, 21, 0))
            .category(Category.MOVIE)
            .performanceStatus(false)
            .deletedFlag(false)
            .reservationStartTime(LocalDateTime.of(2025, 5, 12, 19, 0))
            .file(mockFile)
            .build()

        mockTask = BookScheduledTask.builder()
            .id(1L)
            .bookId(1L)
            .taskStatus(TaskStatus.PENDING)
            .scheduledTime(LocalDateTime.now())
            .build()

        mockGrade = Grade.builder()
            .id(1L)
            .grade("A")
            .place(place)
            .defaultPrice(5000)
            .seatCount(10)
            .build()

        mockSeat = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.BOOKED)


    }

    @Test
    fun `cancelIfUnpaid - 예매 상태가 CONFIRMED라면 예매를 취소하고 좌석을 AVAILABLE로 변경 후 BookScheduledTask 완료 처리한다`() =
        runTest {

            var mockBook = Book.builder()
                .id(1L)
                .performance(mockPerformance)
                .user(mockUser)
                .totalPrice(20000)
                .quantity(2)
                .bookStatus(BookStatus.CONFIRMED)
                .scheduledTask(mockTask)
                .build()

            var mockBookSeat = BookSeat.builder()
                .id(1L)
                .book(mockBook)
                .seat(mockSeat)
                .build()

            val actualBookId = mockBook.id

            every { bookRepository.findById(actualBookId) } returns Optional.of(mockBook)
            every { bookRepository.save(mockBook) } returns mockBook
            every { bookSeatRepository.findAllByBookIdWithSeat(actualBookId) } returns listOf(
                mockBookSeat
            )
            every { seatRepository.save(mockSeat) } returns mockSeat

            bookScheduleManager.cancelIfUnpaid(actualBookId)

            mockBook.bookStatus shouldBe BookStatus.CANCELED
            mockSeat.seatStatus shouldBe SeatStatus.AVAILABLE
            mockBook.scheduledTask.taskStatus shouldBe TaskStatus.COMPLETED

        }

}
