package com.fifo.ticketing.domain.user.dto

import com.fifo.ticketing.domain.user.entity.Role
import jakarta.validation.constraints.NotEmpty

@JvmRecord
data class SessionUser(
    @JvmField val id: Long,
    @JvmField val username: String,
    @JvmField val role: Role
)
