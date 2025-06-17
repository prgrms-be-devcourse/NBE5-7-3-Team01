package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.performance.dto.PerformanceDetailResponse
import com.fifo.ticketing.domain.performance.dto.PerformanceResponseDto
import com.fifo.ticketing.domain.performance.dto.PerformanceSeatGradeDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper.toDetailResponseDto
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper.toPagePerformanceResponseDto
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PerformanceService(
    private val performanceRepository: PerformanceRepository,
    private val gradeRepository: GradeRepository,
    @Value("\${file.url-prefix}") private val urlPrefix: String
) {

    @Transactional(readOnly = true)
    fun getPerformanceDetail(performanceId: Long): PerformanceDetailResponse {
        val performance = performanceRepository.findById(performanceId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }

        val grades = gradeRepository.findAllByPlaceId(performance.place.id!!)
        val seatGrades: List<PerformanceSeatGradeDto> =
            grades.map(PerformanceMapper::toSeatGradeDto)

        return toDetailResponseDto(performance, seatGrades, urlPrefix)
    }

    @Transactional(readOnly = true)
    fun getPerformancesSortedByLatest(pageable: Pageable): Page<PerformanceResponseDto> {
        val performances = performanceRepository.findUpcomingPerformancesOrderByStartTime(
            LocalDateTime.now(),
            pageable
        )
        return toPagePerformanceResponseDto(performances, urlPrefix)
    }

    @Transactional(readOnly = true)
    fun searchPerformancesByKeyword(
        keyword: String,
        pageable: Pageable
    ): Page<PerformanceResponseDto> {
        val performances = performanceRepository.findUpcomingPerformancesByKeywordContaining(
            LocalDateTime.now(),
            keyword,
            pageable
        )
        return toPagePerformanceResponseDto(performances, urlPrefix)
    }

    @Transactional(readOnly = true)
    fun getPerformancesSortedByLikes(pageable: Pageable): Page<PerformanceResponseDto> {
        val performances = performanceRepository.findUpcomingPerformancesOrderByLikes(
            LocalDateTime.now(),
            pageable
        )
        return toPagePerformanceResponseDto(performances, urlPrefix)
    }

    @Transactional(readOnly = true)
    fun getPerformancesByReservationPeriod(
        start: LocalDateTime,
        end: LocalDateTime,
        pageable: Pageable
    ): Page<PerformanceResponseDto> {
        val performances =
            performanceRepository.findUpcomingPerformancesByReservationPeriod(start, end, pageable)
        return toPagePerformanceResponseDto(performances, urlPrefix)
    }

    @Transactional(readOnly = true)
    fun getPerformancesByCategory(
        category: Category,
        pageable: Pageable
    ): Page<PerformanceResponseDto> {
        val performances = performanceRepository.findUpcomingPerformancesByCategory(
            LocalDateTime.now(),
            category,
            pageable
        )
        return toPagePerformanceResponseDto(performances, urlPrefix)
    }
}