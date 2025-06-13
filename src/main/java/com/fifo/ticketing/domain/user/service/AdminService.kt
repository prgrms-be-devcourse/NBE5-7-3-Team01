package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.UserDto
import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.mapper.UserMapper.toUserDtoPage
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getAllUsers(pageable: Pageable): Page<UserDto> {
        val allUserInfo = userRepository.findAll(pageable)
        return toUserDtoPage(allUserInfo)
    }

    @Transactional(readOnly = true)
    fun getUsersByName(pageable: Pageable, name: String): Page<UserDto> {
        val byUsernameContaining = userRepository.findByUsernameContaining(name, pageable)
        return toUserDtoPage(byUsernameContaining)
    }

    @Transactional(readOnly = true)
    fun getUsersByRole(pageable: Pageable, role: Role): Page<UserDto> {
        val byRole = userRepository.findByRole(role, pageable)
        return toUserDtoPage(byRole)
    }

    @Transactional(readOnly = true)
    fun getUsersByRoleAndName(pageable: Pageable, role: Role, name: String): Page<UserDto> {
        val byUsernameContainingAndRole = userRepository.findByUsernameContainingAndRole(
            name, role, pageable
        )
        return toUserDtoPage(byUsernameContainingAndRole)
    }

    @Transactional
    fun updateUserStatus(userId: Long) {
        val user = userRepository.findById(userId).orElseThrow {
            ErrorException(
                ErrorCode.NOT_FOUND_MEMBER
            )
        }
        user.updateBlockedState()
    }
}
