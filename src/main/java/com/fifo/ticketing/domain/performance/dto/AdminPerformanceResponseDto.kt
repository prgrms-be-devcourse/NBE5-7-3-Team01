package com.fifo.ticketing.domain.performance.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공연 응답 DTO (관리자)")
data class AdminPerformanceResponseDto (
    val id: Long,
    val encodedFileName: String,
    val title: String,
    val description: String,
    val category: String,
    val place: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservationStartTime: LocalDateTime,
    val performanceStatus: Boolean,
    val urlPrefix: String
) {
    val url: String
        get() = urlPrefix + encodedFileName
}
