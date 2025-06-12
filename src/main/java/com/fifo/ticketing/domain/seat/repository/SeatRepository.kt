package com.fifo.ticketing.domain.seat.repository

import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SeatRepository : JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s JOIN FETCH s.grade WHERE s.performance.id = :performanceId AND s.seatStatus <> 'DELETED'")
    fun findValidSeatsByPerformanceId(@Param("performanceId") performanceId: Long): List<Seat>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Seat s SET s.seatStatus = :status WHERE s.performance.id = :performanceId")
    fun updateSeatStatusByPerformanceId(
        @Param("performanceId") performanceId: Long,
        @Param("status") status: SeatStatus?
    )

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM Seat s WHERE s.id IN :seatIds")
    fun findAllByIdInWithOptimisticLock(@Param("seatIds") seatIds: List<Long>): List<Seat>

    //공연의 남은 좌석 수 계산
    @Query("select count(*) From Seat s where s.performance.id = :performanceId AND s.seatStatus = 'AVAILABLE'")
    fun countAvailableSeatsByPerformanceId(@Param("performanceId") performanceId: Long): Int
}
