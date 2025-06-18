package com.fifo.ticketing.domain.user.controller.api

import com.fifo.ticketing.domain.user.dto.form.AuthEmailRequest
import com.fifo.ticketing.domain.user.dto.form.SendEmailRequest
import com.fifo.ticketing.domain.user.service.EmailAuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpSession
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
@Tag(name = "Email", description = "이메일 인증 API")
class EmailController(
    private val emailAuthService: EmailAuthService
) {

    @PostMapping("/email/send")
    @Operation(summary = "공연 등록", description = "이메일 정보(sendEmailRequest)를 이용하여 인증 이메일을 송신합니다.")
    @Throws(MessagingException::class)
    fun emailSend(@RequestBody sendEmailRequest: SendEmailRequest): ResponseEntity<*> {
        emailAuthService.sendEmail(sendEmailRequest.email)
        return ResponseEntity.ok().build<Any>()
    }

    @PostMapping("/email/auth")
    @Operation(summary = "공연 등록", description = "이메일 정보(sendEmailRequest)를 이용하여 인증 이메일을 송신합니다.")
    fun emailAuth(
        @RequestBody authEmailRequest: AuthEmailRequest,
        session: HttpSession
    ): ResponseEntity<Void> {
        val checked = emailAuthService.checkAuthCode(
            authEmailRequest.email,
            authEmailRequest.authCode,
            session
        )
        return if (checked) ResponseEntity.ok().build() else ResponseEntity.notFound().build()
    }
}

