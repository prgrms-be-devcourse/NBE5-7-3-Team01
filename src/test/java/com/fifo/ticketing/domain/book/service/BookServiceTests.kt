package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.dto.BookUserDetailDto
import com.fifo.ticketing.domain.book.dto.BookedView
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.mapper.toBookedViewDtoList
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
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.*

class BookServiceTests {

    var urlPrefix: String = ""

    val bookRepository = mockk<BookRepository>()
    val bookSeatRepository = mockk<BookSeatRepository>()
    val bookScheduleManager = mockk<BookScheduleManager>()
    val userRepository = mockk<UserRepository>()
    val performanceRepository = mockk<PerformanceRepository>()
    val seatService = mockk<SeatService>()

    val bookService = BookService(
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
        // 확장 함수 mocking
        mockkStatic("com.fifo.ticketing.domain.book.mapper.BookMapperKt")

        urlPrefix = "https://picsum.photos/200"
        ReflectionTestUtils.setField(bookService, "urlPrefix", urlPrefix)

        mockUser = User(
            id = 1L,
            email = "example@gmail.com",
            password = "123",
            username = "테스트 유저",
            provider = null,
            role = Role.USER,
            isBlocked = false
        )

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

        mockBook = Book(
            id = 1L,
            performance = mockPerformance,
            user = mockUser,
            totalPrice = 20000,
            quantity = 2,
            bookStatus = BookStatus.CONFIRMED
        )

        mockGrade = Grade(1L, place, "A", 5000, 10)

        mockSeat = Seat(1L, mockPerformance, "A1", 5000, mockGrade, SeatStatus.BOOKED)

        mockBookSeat = BookSeat(
            id = 1L,
            book = mockBook,
            seat = mockSeat,
        )

        pageable = PageRequest.of(0, 5)
    }

    @Test
    fun `cancelBookByAdmin - 적절한 값이 들어오면 예매가 취소된다`() {
        val actualBookId = mockBook.id!!

        every { bookRepository.findById(actualBookId) } returns Optional.of(mockBook)
        every { bookSeatRepository.findAllByBookId(actualBookId) } returns listOf(mockBookSeat)
        every { seatService.changeSeatStatus(any(), any()) } just Runs


        bookService.cancelBookByAdmin(actualBookId)

        mockBook.bookStatus shouldBe BookStatus.CANCELED

        verify { seatService.changeSeatStatus(listOf(mockBookSeat), SeatStatus.AVAILABLE) }

    }

    @Test
    fun `cancelBookByAdmin - 존재하지 않는 예매이면 NOT_FOUND_BOOK을 던진다`() {
        val bookId = 999L
        every { bookRepository.findById(bookId) } returns Optional.empty()

        val error = assertThrows<ErrorException> {
            bookService.cancelBookByAdmin(bookId)
        }


        error.errorCode shouldBe ErrorCode.NOT_FOUND_BOOK

    }

    @Test
    fun `getBookUserDetail - 적절한 값이 들어오면 book을 찾아 dto에 urlPrefix를 설정한 후 반환한다`() {
        val bookId = 1L
        val performanceId = 1L
        val mockDto = mockk<BookUserDetailDto>()
        val capturedPrefix = slot<String>()

        every { bookRepository.findBookDetailByBookId(bookId, performanceId) } returns mockDto
        every { mockDto.urlPrefix = capture(capturedPrefix) } just Runs
        every { mockDto.urlPrefix } answers { capturedPrefix.captured }

        val result = bookService.getBookUserDetail(bookId, performanceId)

        result shouldBe mockDto
        result.urlPrefix shouldBe urlPrefix
    }

    @Test
    fun `getBookUserDetail - book을 조회할 수 없으면 NOT_FOUND_BOOK 발생`() {
        val bookId = 2323L
        val performanceId = 11223L

        every { bookRepository.findBookDetailByBookId(bookId, performanceId) } returns null

        val error = assertThrows<ErrorException> {
            bookService.getBookUserDetail(bookId, performanceId)
        }

        error.errorCode shouldBe ErrorCode.NOT_FOUND_BOOK

    }

    @Test
    fun `getBookDetail - 적절한 값이 들어오면 book을 찾아 BookDetail로 변환하여 반환한다`() {
        val actualUserId = mockUser.id
        val actualBookId = mockBook.id!!
        val actualPerformanceId = mockPerformance.id

        every { bookRepository.findByUserIdAndId(actualUserId!!, actualBookId) } returns mockBook

        val expectedBookDetail = bookService.getBookDetail(actualUserId!!, actualBookId)

        expectedBookDetail.bookId shouldBe actualBookId
        expectedBookDetail.performanceId shouldBe actualPerformanceId

    }

    @Test
    fun `getBookDetail - 존재하지 않는 예매이면 NOT_FOUND_BOOK을 던진다`() {
        val unavailableUserId = 99L
        val actualBookId = mockBook.id!!

        every { bookRepository.findByUserIdAndId(unavailableUserId, actualBookId) } returns null

        val error = assertThrows<ErrorException> {
            bookService.getBookDetail(
                unavailableUserId,
                actualBookId
            )
        }

        error.errorCode shouldBe ErrorCode.NOT_FOUND_BOOK

    }

    @Test
    fun `getBookedList - title과 Status 둘 다 있는 경우`() {
        val userId = 1L
        val title = "라따뚜이"
        val status = BookStatus.CONFIRMED
        val mockBookPage = PageImpl(listOf(mockBook), pageable, 1)
        val bookedViewPage = mockk<Page<BookedView>>(relaxed = true)

        // 확장 함수 mocking
        mockkStatic("com.fifo.ticketing.domain.book.mapper.BookMapperKt")


        every { bookRepository.findAllByUserIdAndTitleAndBookStatus(userId, title, status, pageable) } returns mockBookPage
        every { mockBookPage.toBookedViewDtoList(urlPrefix) } returns bookedViewPage

        val result = bookService.getBookedList(userId, title, status, pageable)

        bookedViewPage shouldBe result
        verify { bookRepository.findAllByUserIdAndTitleAndBookStatus(userId, title, status, pageable) }

    }

    @Test
    fun `getBookedList - title과 Status 둘 다 없는 경우`() {
        val userId = 1L
        val title: String? = null
        val status: BookStatus? = null
        val mockBookPage = PageImpl(listOf(mockBook), pageable, 1)
        val bookedViewPage = mockk<Page<BookedView>>(relaxed = true)

        every { bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable) } returns mockBookPage
        every { mockBookPage.toBookedViewDtoList(urlPrefix) } returns bookedViewPage

        val result = bookService.getBookedList(userId, title, status, pageable)

        bookedViewPage shouldBe result
        verify { bookRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable) }
        verify { mockBookPage.toBookedViewDtoList(urlPrefix) }

    }

    @Test
    fun `cancelBook - 적절한 값이 들어오면 예매가 취소되고 좌석이 풀려난다`() {
        val actualUserId = mockUser.id
        val actualBookId = mockBook.id!!

        every { bookRepository.findByUserIdAndId(actualUserId!!, actualBookId) } returns mockBook
        every { bookSeatRepository.findAllByBookId(actualBookId) } returns listOf(mockBookSeat)
        every { seatService.changeSeatStatus(listOf(mockBookSeat), SeatStatus.AVAILABLE) } returns Unit

        val cancelBookId = bookService.cancelBook(actualBookId, actualUserId!!)

        cancelBookId shouldBe actualBookId
        mockBook.bookStatus shouldBe BookStatus.CANCELED

    }

    @Test
    fun `cancelAllBook - 공연 값이 주어지면 해당 공연 모든 예매 취소`() {

        val books = listOf(mockBook)

        every { bookRepository.cancelAllByPerformance(mockPerformance, BookStatus.ADMIN_REFUNDED, BookStatus.PAYED) } just Runs
        every { bookRepository.findAllWithUserAndPerformanceByPerformanceAndBookStatus(
            mockPerformance, BookStatus.ADMIN_REFUNDED
        ) } returns books

        val result = bookService.cancelAllBook(mockPerformance)

        result shouldBe books
        verify {
            bookRepository.cancelAllByPerformance(mockPerformance, BookStatus.ADMIN_REFUNDED, BookStatus.PAYED)
            bookRepository.findAllWithUserAndPerformanceByPerformanceAndBookStatus(mockPerformance, BookStatus.ADMIN_REFUNDED)
        }

    }

}
