package com.fifo.ticketing.domain.user.controller

import com.fifo.ticketing.domain.user.dto.form.SignUpForm
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@ActiveProfiles("ci")
@AutoConfigureMockMvc
internal class SignUpTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun signUpTest_Success() {
        val testForm = SignUpForm(
            "test@test.com",
            "testUser",
            "1234"
        )

        mockMvc.perform(
            post("/users/signup")
                .sessionAttr("emailVerified", "test@test.com")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", testForm.username)
                .param("email", testForm.email)
                .param("password", testForm.password)
        ).andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/users/signin?signupSuccess=true"))
    }

    @Test
    fun signUpTest_Failure_email_verified() {
        val wrongForm = SignUpForm(
            "test@test.com",
            "testUser",
            "1234"
        )

        mockMvc.perform(
            post("/users/signup")
                .sessionAttr("emailVerified", "wrong@test.com")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", wrongForm.username)
                .param("email", wrongForm.email)
                .param("password", wrongForm.password)
        ).andExpect(status().isOk())
            .andExpect(view().name("user/sign_up"))
    }
}
