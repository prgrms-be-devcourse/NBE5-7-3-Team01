package com.fifo.ticketing.domain.user.service

import com.fifo.ticketing.domain.user.dto.form.SignUpForm
import com.fifo.ticketing.domain.user.dto.form.UserFormDetails
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class UserFormService : UserDetailsService {
    private val userRepository: UserRepository? = null
    private val passwordEncoder: PasswordEncoder? = null

    fun save(signUpForm: SignUpForm) {
        if (userRepository!!.findByEmail(signUpForm.email!!).isPresent()) {
            throw ErrorException(ErrorCode.EMAIL_ALREADY_EXISTS)
        }
        val user = User.builder()
            .username(signUpForm.username)
            .password(passwordEncoder!!.encode(signUpForm.password))
            .email(signUpForm.email)
            .build()
        userRepository.save(user)
    }

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val userOptional = userRepository!!.findByEmail(email)
        val findUser: User = userOptional.orElseThrow {
            ErrorException(
                ErrorCode.NOT_FOUND_MEMBER
            )
        }
        return UserFormDetails(findUser)
    }
}
