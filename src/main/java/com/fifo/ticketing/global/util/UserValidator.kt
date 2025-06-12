package com.fifo.ticketing.global.util

import com.fifo.ticketing.domain.user.dto.SessionUser
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.servlet.http.HttpSession

object UserValidator {

    @JvmStatic
    fun validateSessionUser(session: HttpSession): SessionUser {
        val user = session.getAttribute("loginUser") as? SessionUser
            ?: throw ErrorException(ErrorCode.UNAUTHORIZED_REQUEST, "user/sign_in")
        return user
    }
}
