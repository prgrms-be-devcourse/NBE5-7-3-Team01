package com.fifo.ticketing.domain.performance.dto

import com.fifo.ticketing.domain.performance.entity.Category
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "공연 요청 DTO")
data class PerformanceRequestDto(
    var title: String,
    var description: String,
    var category: Category,

    // true : 예매 가능 / false : 예매 불가능
    var performanceStatus: Boolean,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var reservationStartTime: LocalDateTime,
    var placeId: Long,
)
