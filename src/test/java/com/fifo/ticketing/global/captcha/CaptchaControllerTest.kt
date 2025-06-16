package com.fifo.ticketing.global.captcha

import com.fasterxml.jackson.databind.ObjectMapper
import com.fifo.ticketing.global.captcha.dto.CaptchaVerifyRequest
import com.fifo.ticketing.global.service.RedisService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(CaptchaController::class)
class CaptchaControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var redisService: RedisService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    @WithMockUser
    fun `captcha 생성 테스트`() {
        mockMvc.get("/api/captcha")
            .andExpect {
                status { isOk() }
                jsonPath("$.captchaId") { exists() }
                jsonPath("$.image") { value(org.hamcrest.Matchers.startsWith("data:image/png;base64,")) }
            }
    }

    @Test
    @WithMockUser
    fun `captcha 검증 성공`() {
        val captchaId = "captcha:test"
        val input = "TEST12"

        whenever(redisService.getValues(captchaId)).thenReturn(input)

        val request = CaptchaVerifyRequest(captchaId, input)

        mockMvc.post("/api/captcha/verify") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            content { string("올바른 입력입니다.") }
        }
    }

    @Test
    @WithMockUser
    fun `captcha 검증 실패`() {
        val captchaId = "captcha:test"

        whenever(redisService.getValues(captchaId)).thenReturn(null)

        val request = CaptchaVerifyRequest(captchaId, "WRONG1")

        mockMvc.post("/api/captcha/verify") {
            with(csrf())
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isForbidden() }
            content { string("유효 시간이 만료되었습니다.") }
        }
    }


}