package com.fifo.ticketing.domain.user.dto

import com.fifo.ticketing.domain.user.entity.Role
import lombok.Builder
import lombok.Getter

@Getter
@Builder
class UserDto {
    private val id: Long? = null
    private val username: String? = null
    private val email: String? = null
    private val isBlocked: Boolean? = null
    private val role: Role? = null
}
