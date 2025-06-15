package com.fifo.ticketing.domain.user.service.handler

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class FormLoginFailureHandler : AuthenticationFailureHandler {

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = "이메일 또는 비밀번호가 올바르지 않습니다."

        request.session.setAttribute("errormessage", errorMessage)
        response.sendRedirect("/users/signin")
    }
}
