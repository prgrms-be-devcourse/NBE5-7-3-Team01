package com.fifo.ticketing.domain.user.dto

import com.fifo.ticketing.domain.user.entity.Role
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SessionUser(
    val id: @NotEmpty(message = "id는 필수입니다.") Long?,
    val username: @NotEmpty(message = "유저명은 필수입니다.") String?,
    val role: @NotEmpty(message = "유저권한은 필수입니다.") Role?
)
