package com.fifo.ticketing.domain.user.dto.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SendEmailRequest(@JvmField val email: @Email @NotEmpty(message = "이메일을 입력해주세요") String?)
