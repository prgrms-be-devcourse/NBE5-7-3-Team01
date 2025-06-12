package com.fifo.ticketing.domain.user.aop

import com.fifo.ticketing.domain.user.dto.SessionUser
import jakarta.servlet.http.HttpSession
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class LoginUserAttributeAdvice {

    @ModelAttribute
    fun addUserIdToModel(session: HttpSession, model: Model) {
        val loginUser = session.getAttribute("loginUser") as? SessionUser
        if (loginUser != null) {
            model.addAttribute("userId", loginUser.id)
            model.addAttribute("userRole", loginUser.role)
        }
    }
}
