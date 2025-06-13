package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.form.SignUpForm
import com.fifo.ticketing.domain.user.dto.form.UserFormDetails
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.transaction.Transactional
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Transactional
class UserFormService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    fun save(signUpForm: SignUpForm) {
        if (userRepository.findByEmail(signUpForm.email) != null) {
            throw ErrorException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        val user = User.fromForm(
            signUpForm.email,
            passwordEncoder.encode(signUpForm.password),
            signUpForm.username
        )
        userRepository.save(user)
    }

    override fun loadUserByUsername(email: String): UserDetails {
        val findUser = userRepository.findByEmail(email)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_MEMBER)
        return UserFormDetails(findUser)
    }
}
