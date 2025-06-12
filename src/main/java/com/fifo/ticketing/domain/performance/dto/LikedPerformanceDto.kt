package com.fifo.ticketing.domain.performance.dto

import java.time.LocalDateTime

data class LikedPerformanceDto(
    val id: Long? = null,
    val title: String? = null,
    val encodedFileName: String? = null,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null,
    val placeName: String? = null,
    val urlPrefix: String? = null,
) {
    val url: String
        get() = urlPrefix + encodedFileName
}
