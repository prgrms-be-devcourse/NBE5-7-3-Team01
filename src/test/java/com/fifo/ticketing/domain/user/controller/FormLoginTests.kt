package com.fifo.ticketing.domain.user.controller

import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional


@Transactional
@SpringBootTest
@ActiveProfiles("ci")
@AutoConfigureMockMvc
class FormLoginTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: BCryptPasswordEncoder

    @BeforeEach
    fun setUp() {
        val user = User(
            email = "test@test.com",
            username = "test1",
            password = passwordEncoder.encode("1234")
        )
        userRepository.save(user)
    }

    @Test
    fun form_loginSuccessTest_success() {
        mockMvc.perform(
            post("/users/signin")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@test.com")
                .param("loginPwd", "1234")
        ).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/performances"))
    }

    @Test
    fun form_loginSuccessTest_email_failure() {
        mockMvc.perform(
            post("/users/signin")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "wrong@test.com")
                .param("loginPwd", "1234")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/signin"))
    }

    @Test
    fun form_loginSuccessTest_pw_failure() {
        mockMvc.perform(
            post("/users/signin")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "test@test.com")
                .param("loginPwd", "wrongPassword")
        )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/signin"))
    }
}
