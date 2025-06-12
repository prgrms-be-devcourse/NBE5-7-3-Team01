package com.fifo.ticketing.domain.user.dto.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SignUpForm(
    @field:Email @field:NotEmpty(message = "이메일은 필수 입니다.")
    @JvmField val email: String?,

    @field:NotEmpty(message = "유저명은 필수 입니다.")
    @JvmField val username:  String?,

    @field:NotEmpty(message = "비밀번호는 필수 입니다.")
    @JvmField val password: String?
)
