package com.fifo.ticketing.domain.performance.repository

import com.fifo.ticketing.domain.performance.dto.AdminPerformanceStaticsDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Performance
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PerformanceAdminRepository : JpaRepository<Performance, Long> {
    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.deletedFlag != true " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.deletedFlag != true"
    )
    fun findUpcomingPerformancesOrderByReservationStartTimeForAdmin(
        pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.startTime BETWEEN :startDate AND :endDate " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime BETWEEN :startDate AND :endDate"
    )
    fun findUpcomingPerformancesByReservationPeriodForAdmin(
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?,
        pageable: Pageable
    ): Page<Performance>

    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.category = :category " +
                "AND p.deletedFlag != true " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT count(p) FROM Performance p " +
                "WHERE p.deletedFlag != true"
    )
    fun findUpcomingPerformancesByCategoryForAdmin(
        @Param("category") category: Category, pageable: Pageable
    ): Page<Performance>


    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.deletedFlag = true " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT count(p) FROM Performance p " +
                "WHERE p.deletedFlag = true"
    )
    fun findUpComingPerformancesByDeletedFlagForAdmin(
        pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "LEFT JOIN LikeCount lc ON lc.performance = p " +
                "WHERE p.deletedFlag != true " +
                "ORDER BY COALESCE(lc.likeCount, 0) DESC, p.reservationStartTime ASC"),
        countQuery = "SELECT count(p) FROM Performance p " +
                "WHERE p.deletedFlag != true"
    )
    fun findUpcomingPerformancesOrderByLikesForAdmin(
        pageable: Pageable
    ): Page<Performance>

    // 삭제를 위해 '삭제되지 않은 공연'을 조회
    fun findByIdAndDeletedFlagFalse(id: Long): Optional<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.title LIKE CONCAT('%', :keyword, '%') " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.title LIKE CONCAT('%', :keyword, '%') "
    )
    fun findUpcomingPerformancesByKeywordContainingForAdmin(
        @Param("now") now: LocalDateTime,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Performance>

    @Query(
        ("SELECT p.id AS performanceId, p.title AS title, pl.totalSeats AS totalSeats, "
                + "SUM(CASE WHEN b.bookStatus IN ('CONFIRMED', 'PAYED') THEN b.quantity ELSE 0 END) AS reservationCount FROM Performance p "
                + "JOIN Place pl ON p.place.id = pl.id "
                + "LEFT JOIN Book b ON p.id = b.performance.id "
                + "WHERE p.performanceStatus = true "
                + "GROUP BY p.id, p.title, pl.totalSeats")
    )
    fun findPerformanceStatics(
        pageable: Pageable
    ): Page<AdminPerformanceStaticsDto>
}