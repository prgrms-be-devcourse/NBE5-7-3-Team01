package com.fifo.ticketing.global.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

@ExtendWith(MockKExtension::class)
class RedisServiceTest {

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var valueOps: ValueOperations<String, String>

    private lateinit var redisService: RedisService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        redisService = RedisService(redisTemplate)
    }

    @Test
    fun setValue() {
        every { redisTemplate.opsForValue() } returns valueOps
        every { valueOps.set("captcha:random", "randomNum", 3000L, TimeUnit.MILLISECONDS) } just runs

        redisService.setValuesWithTimeout("captcha:random", "randomNum", 3000L)

        verify {
            valueOps.set("captcha:random", "randomNum", 3000L, TimeUnit.MILLISECONDS)
        }
    }

    @Test
    fun readValue() {
        every { redisTemplate.opsForValue() } returns valueOps
        every { valueOps["captcha:random"] } returns "randomNum"

        val result = redisService.getValues("captcha:random")
        assertThat(result).isEqualTo("randomNum")
    }

    @Test
    fun deleteValue() {

        every { redisTemplate.delete("captcha:random") } returns true

        redisService.deleteValues("captcha:random")

        verify {
            redisTemplate.delete("captcha:random")
        }
    }
}