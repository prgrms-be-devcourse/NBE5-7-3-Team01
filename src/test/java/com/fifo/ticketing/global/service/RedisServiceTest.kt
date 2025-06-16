package com.fifo.ticketing.global.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class RedisServiceTest {

    @Mock
    lateinit var redisTemplate: RedisTemplate<String, String>

    @Mock
    lateinit var valueOps: ValueOperations<String, String>

    lateinit var redisService: RedisService

    @BeforeEach
    fun setup() {
        redisService = RedisService(redisTemplate)
    }

    @Test
    fun setValue() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        redisService.setValuesWithTimeout("captcha:random", "randomNum", 3000L)
        verify(valueOps).set("captcha:random", "randomNum", 3000L, TimeUnit.MILLISECONDS)
    }

    @Test
    fun readValue() {
        whenever(redisTemplate.opsForValue()).thenReturn(valueOps)
        whenever(valueOps["captcha:random"]).thenReturn("randomNum")

        val result = redisService.getValues("captcha:random")
        assertThat(result).isEqualTo("randomNum")
    }

    @Test
    fun deleteValue() {
        redisService.deleteValues("captcha:random")

        verify(redisTemplate).delete("captcha:random")
    }
}