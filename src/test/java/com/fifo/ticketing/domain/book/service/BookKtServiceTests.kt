package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.repository.BookSeatRepository
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

class BookKtServiceTests {

    var urlPrefix: String = ""

    val bookRepository = mockk<BookRepository>()
    val bookSeatRepository = mockk<BookSeatRepository>()
    val bookScheduleManager = mockk<BookScheduleManager>()
    val userRepository = mockk<UserRepository>()
    val performanceRepository = mockk<PerformanceRepository>()
    val seatService = mockk<SeatService>()


    val bookKtService = BookKtService(
        urlPrefix,
        bookRepository,
        bookSeatRepository,
        bookScheduleManager,
        userRepository,
        performanceRepository,
        seatService
    )


    private lateinit var mockUser: User
    private lateinit var place: Place
    private lateinit var mockFile: File
    private lateinit var mockPerformance: Performance
    private lateinit var mockBook: Book
    private lateinit var mockGrade: Grade
    private lateinit var mockSeat: Seat
    private lateinit var mockBookSeat: BookSeat
    private lateinit var pageable: PageRequest

    @BeforeEach
    fun setUp() {
        urlPrefix = "https://picsum.photos/200"
        ReflectionTestUtils.setField(bookKtService, "urlPrefix", urlPrefix)

        mockUser = User.builder()
            .id(1L)
            .email("example@gmail.com")
            .password("123")
            .username("테스트 유저")
            .build()

        place = Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100)

        mockFile = File(1L, "poster.jpg", "sample.jpg")

        mockPerformance = Performance(
            1L, "라따뚜이", "라따뚜이는 픽시의 영화입니다.", place,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            Category.MOVIE,
            false,
            false,
            LocalDateTime.of(2025, 5, 12, 19, 0),
            mockFile
        )

        mockBook = Book.builder()
            .id(1L)
            .performance(mockPerformance)
            .user(mockUser)
            .totalPrice(20000)
            .quantity(2)
            .bookStatus(BookStatus.CONFIRMED)
            .build()

        mockGrade = Grade(1L, place, "A", 5000, 10)

        mockSeat = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.BOOKED)

        mockBookSeat = BookSeat.builder()
            .id(1L)
            .book(mockBook)
            .seat(mockSeat)
            .build()

        pageable = PageRequest.of(0, 5)
    }

    @Test
    fun `getBookDetail - 적절한 값이 들어오면 book을 찾아 BookDetail로 변환하여 반환한다`() {
        val actualUserId = mockUser.id
        val actualBookId = mockBook.id
        val actualPerformanceId = mockPerformance.id

        every { bookRepository.findByUserIdAndId(actualUserId, actualBookId) } returns mockBook

        val expectedBookDetail = bookKtService.getBookDetail(actualUserId, actualBookId)

        expectedBookDetail.bookId shouldBe actualBookId
        expectedBookDetail.performanceId shouldBe actualPerformanceId

    }

    @Test
    fun `getBookDetail - 잘못된 값이 들어오면 NOT_FOUND_BOOK을 던진다`() {
        val unavailableUserId = 99L
        val actualBookId = mockBook.id

        every { bookRepository.findByUserIdAndId(unavailableUserId, actualBookId) } returns null

        val error = assertThrows<ErrorException> {
            bookKtService.getBookDetail(
                unavailableUserId,
                actualBookId
            )
        }

        error.errorCode shouldBe ErrorCode.NOT_FOUND_BOOK

    }

    @Test
    fun `cancelBook - 적절한 값이 들어오면 예매가 취소되고 좌석이 풀려난다`() {
        val actualUserId = mockUser.id
        val actualBookId = mockBook.id

        every { bookRepository.findByUserIdAndId(actualUserId, actualBookId) } returns mockBook
        every { bookSeatRepository.findAllByBookId(actualBookId) } returns listOf(mockBookSeat)

        val cancelBookId = bookKtService.cancelBook(actualBookId, actualUserId)

        cancelBookId shouldBe actualBookId
        mockBook.bookStatus shouldBe BookStatus.CANCELED

    }

}

