package com.fifo.ticketing.domain.book.controller.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.fifo.ticketing.domain.book.dto.BookCreateRequest
import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.global.service.MailService
import jakarta.servlet.http.HttpSession
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestSessionController {
    @PostMapping("/test-session")
    fun setTestSession(session: HttpSession): ResponseEntity<Void> {
        val mockUser = SessionUser(1L, "테스트", Role.USER)
        session.setAttribute("loginUser", mockUser)
        return ResponseEntity.ok().build()
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("ci")
class BookControllerTests {

    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockitoBean
    lateinit var bookService: BookService

    @Test
    fun `createBook - 예매 생성 요청을 보내면 createBook 메서드가 정상적으로 실행된다`() = runTest {

        // given
        val performanceId = 1L
        val seatIds = listOf(101L, 102L)
        val userId = 1L
        val bookId = 1234L

        val request = BookCreateRequest(seatIds)

        val sessionCookie = webTestClient.post()
            .uri("/test-session")
            .exchange()
            .returnResult<Void>()
            .responseCookies["JSESSIONID"]?.first()
        `when`(bookService.createBook(performanceId, userId, request))
            .thenReturn(bookId)

        // when
        webTestClient.post()
            .uri("/performances/$performanceId/book?seatIds=101&seatIds=102")
            .cookie("JSESSIONID", sessionCookie?.value ?: "dummy")
            .exchange()
            .expectStatus().is3xxRedirection
            .expectHeader()
            .valueMatches("Location", ".*/performances/$performanceId/book/complete/$bookId")

        // then
        verify(bookService).createBook(performanceId, userId, request)

    }
}