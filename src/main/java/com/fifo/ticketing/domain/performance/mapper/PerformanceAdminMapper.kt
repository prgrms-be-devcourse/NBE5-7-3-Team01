package com.fifo.ticketing.domain.performance.mapper

import com.fifo.ticketing.domain.performance.dto.AdminPerformanceDetailResponse
import com.fifo.ticketing.domain.performance.dto.AdminPerformanceResponseDto
import com.fifo.ticketing.domain.performance.dto.PerformanceSeatGradeDto
import com.fifo.ticketing.domain.performance.entity.Performance
import org.springframework.data.domain.Page

object PerformanceAdminMapper {
    @JvmStatic
    fun toAdminDetailResponseDto(
        performance: Performance,
        seatGrades: List<PerformanceSeatGradeDto>,
        urlPrefix: String
    ): AdminPerformanceDetailResponse {
        return AdminPerformanceDetailResponse(
            performance.id,
            performance.title,
            performance.description,
            performance.place.name,
            performance.file!!.encodedFileName,
            performance.place.address,
            performance.category.name,
            performance.startTime,
            performance.endTime,
            performance.deletedFlag,
            performance.performanceStatus,
            performance.place.totalSeats,
            seatGrades,
            urlPrefix
        )
    }

    @JvmStatic
    fun toAdminPerformanceResponseDto(
        performance: Performance,
        urlPrefix: String
    ): AdminPerformanceResponseDto {
        return AdminPerformanceResponseDto(
            performance.id!!,
            performance.file!!.encodedFileName,
            performance.title,
            performance.description,
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
    fun toPageAdminPerformanceResponseDto(
        performances: Page<Performance>,
        urlPrefix: String
    ): Page<AdminPerformanceResponseDto> {
        return performances.map {
            toAdminPerformanceResponseDto(it, urlPrefix)
        }
    }
}
