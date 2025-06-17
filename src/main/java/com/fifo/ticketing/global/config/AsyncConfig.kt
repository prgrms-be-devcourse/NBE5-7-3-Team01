package com.fifo.ticketing.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@EnableAsync
@Configuration
class AsyncConfig : AsyncConfigurer {

    @Bean(name = ["mailExecutor"])
    override fun getAsyncExecutor(): Executor =
        Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = ["cancelPerformanceMailExecutor"])
    fun cancelMailExecutor(): Executor =
        Executors.newVirtualThreadPerTaskExecutor()
}
