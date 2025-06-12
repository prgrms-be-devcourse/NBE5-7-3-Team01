package com.fifo.ticketing.domain.performance.dto

import java.time.LocalDateTime

data class AdminPerformanceDetailResponse(
    val performanceId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val placeName: String? = null,
    val encodedFileName: String? = null,
    val address: String? = null,
    val category: String? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val deletedFlag: Boolean = false,
    val performanceStatus: Boolean = false,
    val totalSeats: Int = 0,
    val seatGrades: List<PerformanceSeatGradeDto>? = null,
    val urlPrefix: String? = null,
) {

    val url: String
        get() = urlPrefix + encodedFileName
}
