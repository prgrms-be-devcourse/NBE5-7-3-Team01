package com.fifo.ticketing.domain.user.mapper

import com.fifo.ticketing.domain.user.dto.UserDto
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page

object UserMapper {

    @JvmStatic
    fun toUserDto(user: User): UserDto {
        return UserDto(
            user.id,
            user.username,
            user.email,
            user.isBlocked,
            user.role
        )
    }

    @JvmStatic
    fun toUserDtoPage(users: Page<User>): Page<UserDto> {
        return users.map { toUserDto(it) }
    }
}
