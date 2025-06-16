package com.fifo.ticketing.global.captcha

import com.fasterxml.jackson.databind.ObjectMapper
import com.fifo.ticketing.global.captcha.dto.CaptchaVerifyRequest
import com.fifo.ticketing.global.service.RedisService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class CaptchaControllerTest {

    @Mock
    lateinit var redisService: RedisService

    @InjectMocks
    lateinit var captchaController: CaptchaController

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(captchaController).build()
    }

    var objectMapper: ObjectMapper = ObjectMapper()

    @Test
    fun `captcha 생성 테스트`() {
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

        whenever(redisService.getValues(captchaId)).thenReturn(input)

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

        whenever(redisService.getValues(captchaId)).thenReturn(null)

        val request = CaptchaVerifyRequest(captchaId, "WRONG1")

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/captcha/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden)
    }


}