package com.fifo.ticketing.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@EnableAsync
@Configuration
class AsyncConfig : AsyncConfigurer {

    @Bean(name = ["mailExecutor"])
    override fun getAsyncExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 200
            maxPoolSize = 400
            queueCapacity = 100
            initialize()
        }

    @Bean(name = ["cancelPerformanceMailExecutor"])
    fun cancelMailExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 200
            maxPoolSize = 400
            queueCapacity = 100
            keepAliveSeconds = 60
            threadNamePrefix = "async-exec-mail-"
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
}