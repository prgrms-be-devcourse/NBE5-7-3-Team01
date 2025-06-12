package com.fifo.ticketing.domain.user.repository

import com.fifo.ticketing.domain.user.entity.Role
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?

    fun findByUsernameContaining(userName: String, pageable: Pageable): Page<User>

    fun findByRole(role: Role, pageable: Pageable): Page<User>

    fun findByUsernameContainingAndRole(
        username: String,
        role: Role,
        pageable: Pageable
    ): Page<User>
}
