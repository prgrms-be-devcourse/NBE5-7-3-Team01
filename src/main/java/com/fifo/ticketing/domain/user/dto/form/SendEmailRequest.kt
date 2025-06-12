package com.fifo.ticketing.domain.user.dto.form

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SendEmailRequest(
    @field:Email @field:NotEmpty(message = "이메일을 입력해주세요")
    @JvmField val email:String?)
