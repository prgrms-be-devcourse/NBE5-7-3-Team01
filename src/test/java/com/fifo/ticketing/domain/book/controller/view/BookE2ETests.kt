package com.fifo.ticketing.domain.book.controller.view

import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.mapper.toBookCompleteDto
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.performance.repository.PlaceRepository
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.repository.FileRepository
import com.fifo.ticketing.global.service.MailService
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
class BookMockMvcTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var bookRepository: BookRepository


    @Autowired
    lateinit var performanceRepository: PerformanceRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var bookService: BookService

    @Autowired
    lateinit var seatRepository: SeatRepository

    @Autowired
    lateinit var gradeRepository: GradeRepository

    @Autowired
    lateinit var placeRepository: PlaceRepository

    @Autowired
    lateinit var fileRepository: FileRepository

    @MockitoBean
    lateinit var mailServiceMock: MailService



    private lateinit var user: User
    private lateinit var place: Place
    private lateinit var file: File
    private lateinit var performance: Performance
    private lateinit var book: Book
    private lateinit var grade: Grade
    private lateinit var seat: Seat
    private lateinit var bookSeat: BookSeat

    var urlPrefix: String = ""

    @BeforeEach
    fun setUp() {
        urlPrefix = "https://picsum.photos/200"
        ReflectionTestUtils.setField(bookService, "urlPrefix", urlPrefix)

        user = userRepository.saveAndFlush(
            User(
                email = "example@gmail.com",
                password = "123",
                username = "테스트",
                provider = null,
                role = Role.USER,
                isBlocked = false
            )
        )


        place = placeRepository.saveAndFlush(Place(null, "서울특별시 서초구 서초동 1307", "강남아트홀", 100))
        file = fileRepository.saveAndFlush(File(null, "poster.jpg", "sample.jpg"))

        performance = performanceRepository.saveAndFlush(
            Performance(
                null, "라따뚜이", "라따뚜이는 픽시의 영화입니다.", place,
                LocalDateTime.of(2025, 6, 1, 19, 0),
                LocalDateTime.of(2025, 6, 1, 21, 0),
                Category.MOVIE,
                false,
                false,
                LocalDateTime.of(2025, 5, 12, 19, 0),
                file
            )
        )

        grade = gradeRepository.saveAndFlush(Grade(null, place, "A", 5000, 10))


        book = bookRepository.saveAndFlush(
            Book(
                id = null,
                performance = performance,
                user = user,
                totalPrice = 20000,
                quantity = 2,
                bookStatus = BookStatus.CONFIRMED
            )
        )

        seat = seatRepository.saveAndFlush(
            Seat(
                null,
                performance,
                "A1",
                5000,
                grade,
                SeatStatus.AVAILABLE
            )
        )

        bookSeat = BookSeat(
            id = null,
            book = book,
            seat = seat,
        )

    }

    @Test
    fun `예매 결제 완료 후 리다이렉트 및 메일 전송`() {

        mockMvc.perform(
            post("/performances/${performance.id}/book/complete/${book.id}/paid")
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/performances/${performance.id}/book/complete/${book.id}?paid=true"))
            .andReturn()

        val updatedBook = book.id?.let { bookRepository.findById(it).get() }
        updatedBook!!.bookStatus shouldBe BookStatus.PAYED

        verify(mailServiceMock).sendBookInformationNoticeMail(any())
    }

    @Test
    @WithMockUser(username = "testUser", roles = ["USER"])
    fun `예매 완료 화면 진입시 정보가 담긴 모델이 전달된다`() {

        val bookCompleteInfo = book.toBookCompleteDto(urlPrefix)
        bookCompleteInfo.paymentCompleted = true

        val session = MockHttpSession()
        session.setAttribute("loginUser", SessionUser(user.id!!, user.username, user.role))

        mockMvc.perform(
            get("/performances/${performance.id}/book/complete/${book.id}")
                .param("paid", "true")
                .session(session)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("book/complete"))
            .andExpect(model().attribute("book", bookCompleteInfo))
            .andExpect(model().attribute("bookId", book.id))

    }
}