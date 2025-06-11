package com.fifo.ticketing.domain.user.dto.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SignUpForm(
    @JvmField val email: @Email @NotEmpty(message = "이메일은 필수 입니다.") String?,
    @JvmField val username: @NotEmpty(message = "유저명은 필수 입니다.") String?,
    @JvmField val password: @NotEmpty(message = "비밀번호는 필수 입니다.") String?
)
