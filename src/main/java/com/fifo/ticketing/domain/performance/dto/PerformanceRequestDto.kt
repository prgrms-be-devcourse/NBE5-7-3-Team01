package com.fifo.ticketing.domain.performance.dto

import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.global.validation.ValidPerformanceDates
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@ValidPerformanceDates
@Schema(description = "공연 요청 DTO")
data class PerformanceRequestDto(

    @field:NotBlank(message = "공연 제목은 필수입니다.")
    var title: String,

    @field:NotBlank(message = "공연 설명은 필수입니다.")
    var description: String,

    @field:NotNull(message = "카테고리는 필수입니다.")
    var category: Category,

    // true : 예매 가능 / false : 예매 불가능
    @field:NotNull(message = "예매 가능 여부는 필수입니다.")
    var performanceStatus: Boolean,

    @field:NotNull(message = "시작 시간은 필수입니다.")
    var startTime: LocalDateTime,

    @field:NotNull(message = "종료 시간은 필수입니다.")
    var endTime: LocalDateTime,

    @field:NotNull(message = "예매 시작 시간은 필수입니다.")
    var reservationStartTime: LocalDateTime,

    @field:NotNull(message = "공연장은 필수입니다.")
    var placeId: Long,
)
