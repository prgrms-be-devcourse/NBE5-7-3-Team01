package com.fifo.ticketing.domain.user.dto.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class AuthEmailRequest(
    @JvmField val email: @Email @NotEmpty(message = "이메일을 입력해 주세요") String?,
    @JvmField val authCode: @NotEmpty(message = "인증 번호를 입력해 주세요") String?
)
