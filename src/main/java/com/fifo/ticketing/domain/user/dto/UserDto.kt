package com.fifo.ticketing.domain.user.dto

import com.fifo.ticketing.domain.user.entity.Role
import lombok.Builder
import lombok.Getter

//@Getter
//@Builder
data class UserDto (
    val id: Long?,
    val username: String,
    val email: String,
    val isBlocked: Boolean,
    val role: Role
)
