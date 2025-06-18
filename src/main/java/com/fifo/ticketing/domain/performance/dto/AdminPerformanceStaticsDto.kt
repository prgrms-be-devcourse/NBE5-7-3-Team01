package com.fifo.ticketing.domain.performance.dto

interface AdminPerformanceStaticsDto {
    val performanceId: Long?

    val title: String?

    val totalSeats: Int?

    val reservationCount: Long?
}
