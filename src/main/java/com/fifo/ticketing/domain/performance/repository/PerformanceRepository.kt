package com.fifo.ticketing.domain.performance.repository

import com.fifo.ticketing.domain.performance.dto.AdminPerformanceBookDetailDto
import com.fifo.ticketing.domain.performance.dto.AdminPerformanceStaticsDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Performance
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface PerformanceRepository : JpaRepository<Performance, Long> {
    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false "
                + "AND p.title LIKE CONCAT('%', :keyword, '%') " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false " +
                "AND p.title LIKE CONCAT('%', :keyword, '%')"
    )
    fun findUpcomingPerformancesByKeywordContaining(
        @Param("now") now: LocalDateTime,
        @Param("keyword") keyword: String,
        pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false"
    )
    fun findUpcomingPerformancesOrderByStartTime(
        @Param("now") now: LocalDateTime, pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "LEFT JOIN LikeCount lc ON lc.performance = p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false " +
                "ORDER BY COALESCE(lc.likeCount, 0) DESC, p.reservationStartTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime > :now AND p.deletedFlag = false"
    )
    fun findUpcomingPerformancesOrderByLikes(
        @Param("now") now: LocalDateTime,
        pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.startTime BETWEEN :startDate AND :endDate AND p.deletedFlag = false " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime BETWEEN :startDate AND :endDate " +
                "AND p.deletedFlag = false"
    )
    fun findUpcomingPerformancesByReservationPeriod(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        pageable: Pageable
    ): Page<Performance>

    @EntityGraph(attributePaths = ["file"])
    @Query(
        value = ("SELECT p FROM Performance p " +
                "WHERE p.startTime > :now AND p.category = :category AND p.deletedFlag = false " +
                "ORDER BY p.reservationStartTime ASC, p.startTime ASC"),
        countQuery = "SELECT COUNT(p) FROM Performance p " +
                "WHERE p.startTime > :now AND p.category = :category " +
                "AND p.deletedFlag = false"
    )
    fun findUpcomingPerformancesByCategory(
        @Param("now") now: LocalDateTime,
        @Param("category") category: Category,
        pageable: Pageable
    ): Page<Performance>

    @Query(
        ("SELECT new com.fifo.ticketing.domain.performance.dto.AdminPerformanceBookDetailDto("
                + "p.id, p.title, f.encodedFileName, COALESCE(SUM(b.totalPrice), 0), COALESCE(SUM(b.quantity), 0)) "
                + "FROM Performance p "
                + "LEFT JOIN Book b ON b.performance.id = p.id "
                + "LEFT JOIN File f ON p.file.id = f.id "
                + "WHERE p.deletedFlag = false AND p.id = :performanceId "
                + "GROUP BY p.id, p.title, f.encodedFileName")
    )
    fun findPerformanceBookDetails(
        @Param("performanceId") performanceId: Long
    ): AdminPerformanceBookDetailDto

    @Modifying
    @Query(
        ("UPDATE Performance p " +
                "SET p.performanceStatus = true " +
                "WHERE p.reservationStartTime <= :now AND p.performanceStatus = false ")
    )
    fun updatePerformanceStatusToReservationStart(@Param("now") now: LocalDateTime)
}