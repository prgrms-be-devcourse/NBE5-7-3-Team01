package com.fifo.ticketing.domain.performance.dto

data class PerformanceSeatGradeDto(
    val grade: String? = null,
    val seatCount: Int = 0,
    val defaultPrice : Int = 0
)

