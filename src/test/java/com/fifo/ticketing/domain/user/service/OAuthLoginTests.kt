package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.dto.oauth.UserOAuthDetails
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.domain.user.service.handler.OAuth2LoginFailureHandler
import com.fifo.ticketing.domain.user.service.handler.OAuth2LoginSuccessHandler
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("ci")
@ExtendWith(MockKExtension::class)
internal class OAuthLoginTests {
    @MockK
    private lateinit var session: HttpSession

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var request: HttpServletRequest

    @MockK
    private lateinit var response: HttpServletResponse

    @MockK
    private lateinit var exception: AuthenticationException

    @InjectMockKs
    private lateinit var oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler

    @InjectMockKs
    private lateinit var oAuth2LoginFailureHandler: OAuth2LoginFailureHandler

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { request.getSession() } returns session
        every { request.getSession(false) } returns session
        every { request.session } returns session
        every { session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") } returns null
    }

    @Test
    @DisplayName("기존에 있는 유저 로그인 성공 테스트")
    fun oAuth2LoginSuccessHandler_test_success() {
        val userDetails = UserOAuthDetails(
            username = "테스트 유저",
            email = "test@test.com",
            userAttributes = mapOf(
                "email" to "test@test.com",
                "name" to "테스트 유저"
            ),
            role = Role.USER
        )

        val token = OAuth2AuthenticationToken(
            userDetails,
            userDetails.authorities,
            "google"
        )

        val user = User(
            id = 1L,
            email = "test@test.com",
            username = "테스트 유저",
            provider = "google"
        )

        every { userRepository.findByEmail("test@test.com") } returns user
        every { session.setAttribute("loginUser", SessionUser(1L, "테스트 유저", Role.USER)) } just Runs
        every { response.sendRedirect("/performances") } just Runs

        oAuth2LoginSuccessHandler.onAuthenticationSuccess(request, response, token)

        verify { session.setAttribute("loginUser", SessionUser(1L, "테스트 유저", Role.USER)) }
        verify { response.sendRedirect("/performances") }
    }

    @Test
    @DisplayName("로그인 실패 시 세션에 에러 메세지 저장, 로그인 페이지로 리다이렉트")
    fun oAuth2LoginFailureHandler_test_failure() {

        every { exception.message } returns "소셜 로그인 실패"
        every { session.setAttribute("errormessage", "소셜 로그인 실패") } just Runs
        every { response.sendRedirect("/users/signin") } just Runs

        oAuth2LoginFailureHandler.onAuthenticationFailure(request, response, exception)

        verify { session.setAttribute("errormessage", "소셜 로그인 실패") }
        verify { response.sendRedirect("/users/signin") }
    }
}
