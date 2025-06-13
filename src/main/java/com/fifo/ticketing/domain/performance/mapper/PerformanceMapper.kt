package com.fifo.ticketing.domain.performance.mapper

import com.fifo.ticketing.domain.performance.dto.*
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import org.springframework.data.domain.Page

object PerformanceMapper {
    @JvmStatic
    fun toDetailResponseDto(
        performance: Performance,
        seatGrades: List<PerformanceSeatGradeDto>, urlPrefix: String
    ): PerformanceDetailResponse {
        return PerformanceDetailResponse(
            performance.id!!,
            performance.title,
            performance.description,
            performance.place.name,
            performance.file!!.encodedFileName,
            performance.place.address,
            performance.category.name,
            performance.startTime,
            performance.endTime,
            performance.reservationStartTime,
            performance.performanceStatus,
            performance.place.totalSeats,
            seatGrades,
            urlPrefix
        )
    }

    @JvmStatic
    fun toSeatGradeDto(grade: Grade): PerformanceSeatGradeDto {
        return PerformanceSeatGradeDto(
            grade.grade, grade.defaultPrice,
            grade.seatCount
        )
    }

    @JvmStatic
    fun toPerformanceResponseDto(
        performance: Performance,
        urlPrefix: String
    ): PerformanceResponseDto {
        return PerformanceResponseDto(
            performance.id!!,
            performance.file!!.encodedFileName,
            performance.title,
            performance.category.name,
            performance.place.name,
            performance.startTime,
            performance.endTime,
            performance.reservationStartTime,
            performance.performanceStatus,
            urlPrefix
        )
    }

    @JvmStatic
    fun toEntity(dto: PerformanceRequestDto, place: Place): Performance {
        val dateOnly = dto.reservationStartTime.toLocalDate()
        val fixedReservationStartTime = dateOnly.atTime(13, 0)
        return Performance(
            null,
            dto.title,
            dto.description,
            place,
            dto.startTime,
            dto.endTime,
            dto.category,
            dto.performanceStatus,
            false,
            fixedReservationStartTime,
            null
        )
    }

    @JvmStatic
    fun toPagePerformanceResponseDto(
        performances: Page<Performance>, urlPrefix: String
    ): Page<PerformanceResponseDto> {
        return performances.map { performance: Performance ->
            toPerformanceResponseDto(
                performance,
                urlPrefix
            )
        }
    }

    @JvmStatic
    fun toLikedPerformanceDto(
        performance: Performance,
        prefix: String
    ): LikedPerformanceDto {
        return LikedPerformanceDto(
            performance.id!!,
            performance.title,
            performance.file!!.encodedFileName,
            performance.startTime,
            performance.endTime,
            performance.place.name,
            prefix
        )
    }

    @JvmStatic
    fun toPageLikedPerformanceDto(
        performances: Page<Performance>, urlPrefix: String
    ): Page<LikedPerformanceDto> {
        return performances.map { performance: Performance ->
            toLikedPerformanceDto(
                performance,
                urlPrefix
            )
        }
    }
}
