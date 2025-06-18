package com.fifo.ticketing.global.captcha

import com.fasterxml.jackson.databind.ObjectMapper
import com.fifo.ticketing.global.captcha.dto.CaptchaVerifyRequest
import com.fifo.ticketing.global.service.RedisService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockKExtension::class)
class CaptchaControllerTest {

    @MockK
    private lateinit var redisService: RedisService

    @InjectMockKs
    private lateinit var captchaController: CaptchaController

    private lateinit var mockMvc: MockMvc
    private var objectMapper: ObjectMapper = ObjectMapper()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        captchaController = CaptchaController(redisService)
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build()
    }


    @Test
    fun `captcha 생성 테스트`() {

        every { redisService.setValuesWithTimeout(any(), any(), any()) } returns Unit

        mockMvc.perform(MockMvcRequestBuilders.get("/api/captcha"))
            .andExpect {
                status().isOk
                jsonPath("$.captchaId").exists()
                jsonPath("$.image").exists()
            }
    }

    @Test
    fun `captcha 검증 성공`() {
        val captchaId = "captcha:test"
        val input = "TEST12"

        every { redisService.getValues(captchaId) } returns input
        every { redisService.deleteValues(captchaId) } returns Unit

        val request = CaptchaVerifyRequest(captchaId, input)

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/captcha/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
    }

    @Test
    fun `captcha 검증 실패`() {
        val captchaId = "captcha:test"

        every { redisService.getValues(captchaId) } returns null

        val request = CaptchaVerifyRequest(captchaId, "WRONG1")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/captcha/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden)
    }
}