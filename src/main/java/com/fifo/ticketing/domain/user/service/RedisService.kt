package com.fifo.ticketing.domain.user.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun setValuesWithTimeout(key: String, value: String, timeout: Long) {
        redisTemplate.opsForValue()[key, value, timeout] =
            TimeUnit.MILLISECONDS
    }

    fun getValues(key: String): String? {
        return redisTemplate.opsForValue()[key]
    }

    fun deleteValues(key: String) {
        redisTemplate.delete(key)
    }
}
