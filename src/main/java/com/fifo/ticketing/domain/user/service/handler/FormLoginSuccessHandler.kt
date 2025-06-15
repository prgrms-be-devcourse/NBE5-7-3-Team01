package com.fifo.ticketing.domain.user.service.handler

import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.domain.user.dto.form.UserFormDetails
import com.fifo.ticketing.domain.user.entity.Role
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class FormLoginSuccessHandler : AuthenticationSuccessHandler {
    private val requestCache = HttpSessionRequestCache()

    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val userDetails = authentication.principal as UserFormDetails

        val session = request.session
        session.setAttribute(
            "loginUser",
            SessionUser(userDetails.id!!, userDetails.name, userDetails.role)
        )

        val savedRequest = requestCache.getRequest(request, response)
        when {
            savedRequest != null -> response.sendRedirect(savedRequest.redirectUrl)
            userDetails.role == Role.ADMIN -> response.sendRedirect("/admin/menu")
            else -> response.sendRedirect("/performances")
        }
    }
}
