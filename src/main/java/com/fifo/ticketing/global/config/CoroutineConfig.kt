package com.fifo.ticketing.global.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoroutineConfig {

    @Bean
    fun coroutineScope(): CoroutineScope {
        // SupervisorJob(): 각 코루틴을 독립으로 처리
        // Dispatchers.IO: 코루틴으로 실행하려는 작업이 db 작업이라 IO로 설정
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}