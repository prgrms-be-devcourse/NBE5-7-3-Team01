package com.fifo.ticketing.domain.performance.dto

import java.time.LocalDateTime

data class LikedPerformanceDto(
    val id: Long,
    val title: String,
    val encodedFileName: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val placeName: String,
    val urlPrefix: String,
) {
    val url: String
        get() = urlPrefix + encodedFileName
}
