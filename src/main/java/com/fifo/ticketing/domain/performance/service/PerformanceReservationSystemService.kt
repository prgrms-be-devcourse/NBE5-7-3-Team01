package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PerformanceReservationSystemService(
    private val performanceRepository: PerformanceRepository,
    private val seatRepository: SeatRepository
) {

    @Transactional
    fun updateStatusIfReservationStart() {
        performanceRepository.updatePerformanceStatusToReservationStart(LocalDateTime.now())
    }

    @Transactional
    fun updateStatusIfSoldOutOrCanceled() {
        val now = LocalDateTime.now()
        val activePerformances = performanceRepository.findActivePerformances(now)

        activePerformances.forEach { performance ->
            val performanceId = performance.id
            val availableSeats = seatRepository.countAvailableSeatsByPerformanceId(performanceId!!)

            when {
                availableSeats == 0 -> performanceRepository.updatePerformanceStatusReservationUnavailable(
                    performanceId
                )

                availableSeats > 0 -> performanceRepository.updatePerformanceStatusReservationAvailable(
                    performanceId
                )
            }
        }
    }
}
