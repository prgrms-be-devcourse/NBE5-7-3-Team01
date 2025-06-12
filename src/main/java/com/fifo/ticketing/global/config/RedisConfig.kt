package com.fifo.ticketing.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,

    @Value("\${spring.data.redis.port}")
    private val port: Int
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory =
        LettuceConnectionFactory(host, port)

    @Bean
    @Primary
    fun redisTemplate(): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            setConnectionFactory(redisConnectionFactory())
        }
}
