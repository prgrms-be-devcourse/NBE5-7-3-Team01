package com.fifo.ticketing.domain.performance.service;

import static com.fifo.ticketing.global.exception.ErrorCode.NOT_FOUND_PERFORMANCE;
import static com.fifo.ticketing.global.exception.ErrorCode.NOT_FOUND_PERFORMANCES;

import com.fifo.ticketing.domain.performance.dto.PerformanceDetailResponse;
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper;
import com.fifo.ticketing.domain.performance.dto.PerformanceSeatGradeDto;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Grade;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.repository.GradeRepository;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.global.exception.ErrorException;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final GradeRepository gradeRepository;

    @Transactional(readOnly = true)
    public PerformanceDetailResponse getPerformanceDetail(Long performanceId) {
        Performance performance = performanceRepository.findById(performanceId)
            .orElseThrow(() -> new ErrorException(NOT_FOUND_PERFORMANCE));

        List<Grade> grades = gradeRepository.findByPlaceId(performance.getPlace().getId());
        List<PerformanceSeatGradeDto> seatGrades = grades.stream()
            .map(PerformanceMapper::toSeatGradeDto)
            .toList();

        return PerformanceMapper.toDetailResponseDto(performance, seatGrades);
    }

    @Transactional(readOnly = true)
    public Page<Performance> getPerformancesSortedByLatest(Pageable pageable) {
        Page<Performance> upcomingPerformances = performanceRepository.findUpcomingPerformancesOrderByReservationStartTime(
            LocalDateTime.now(), pageable);
        if (upcomingPerformances.isEmpty()) {
            throw new ErrorException(NOT_FOUND_PERFORMANCES);
        }
        return upcomingPerformances;
    }

    @Transactional(readOnly = true)
    public Page<Performance> getPerformancesSortedByLikes(Pageable pageable) {
        Page<Performance> upcomingPerformances = performanceRepository.findUpcomingPerformancesOrderByLikes(
            LocalDateTime.now(), pageable);
        if (upcomingPerformances.isEmpty()) {
            throw new ErrorException(NOT_FOUND_PERFORMANCES);
        }
        return upcomingPerformances;
    }

    @Transactional(readOnly = true)
    public Page<Performance> getPerformancesByReservationPeriod(LocalDateTime start,
        LocalDateTime end, Pageable pageable) {
        Page<Performance> upcomingPerformances = performanceRepository.findUpcomingPerformancesByReservationPeriod(
            start, end, pageable);
        if (upcomingPerformances.isEmpty()) {
            throw new ErrorException(NOT_FOUND_PERFORMANCES);
        }
        return upcomingPerformances;
    }

    @Transactional(readOnly = true)
    public Page<Performance> getPerformancesByCategory(Category category, Pageable pageable) {
        Page<Performance> upcomingPerformances = performanceRepository.findUpcomingPerformancesByCategory(
            LocalDateTime.now(), category, pageable);
        if (upcomingPerformances.isEmpty()) {
            throw new ErrorException(NOT_FOUND_PERFORMANCES);
        }
        return upcomingPerformances;
    }
}
