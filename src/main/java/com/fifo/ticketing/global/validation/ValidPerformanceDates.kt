package com.fifo.ticketing.global.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPerformanceDatesValidator::class])
annotation class ValidPerformanceDates(
    val message: String =
        "날짜 순서가 올바르지 않습니다.",
    val groups: Array<KClass<*>> = [],                // 반드시 포함
    val payload: Array<KClass<out Payload>> = []      // 반드시
)