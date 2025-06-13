package com.fifo.ticketing.domain.performance.dto

import java.time.LocalDateTime

data class PerformanceDetailResponse(
    val performanceId: Long,
    val title: String,
    val description: String,
    val placeName: String,
    val encodedFileName: String,
    val address: String,
    val category: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservationStartTime: LocalDateTime,
    val performanceStatus: Boolean,
    val totalSeats: Int,
    val seatGrades: List<PerformanceSeatGradeDto>,
    val urlPrefix: String,
) {
    val url: String
        get() = urlPrefix + encodedFileName
}
