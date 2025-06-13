package com.fifo.ticketing.domain.user.service.handler

import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.dto.oauth.UserOAuthDetails
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.repository.UserRepository
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OAuth2LoginSuccessHandler(
    private val userRepository: UserRepository
) : AuthenticationSuccessHandler {

    private val requestCache = HttpSessionRequestCache()

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userDetails = authentication.principal as UserOAuthDetails

        val user = userRepository.findByEmail(userDetails.email)
        request.session
            .setAttribute(
                "loginUser",
                SessionUser(user!!.id, user.username, user.role)
            )

        val savedRequest = requestCache.getRequest(request, response)
        when {
            savedRequest != null -> response.sendRedirect(savedRequest.redirectUrl)
            user.role == Role.ADMIN -> response.sendRedirect("/admin/menu")
            else -> response.sendRedirect("/performances")
        }
    }
}
