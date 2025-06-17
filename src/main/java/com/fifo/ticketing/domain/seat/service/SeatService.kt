package com.fifo.ticketing.domain.seat.service

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.mapper.toBookSeatViewDto
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Service
import java.util.function.Function
import java.util.stream.Collectors

@Service
class SeatService(
    val seatRepository: SeatRepository,
    val entityManager: EntityManager,

) {

    fun changeSeatStatus(bookSeats: List<BookSeat>, newStatus: SeatStatus) {
        for (bookSeat in bookSeats) {
            val seat = bookSeat.seat
            when (newStatus) {
                SeatStatus.OCCUPIED -> seat.occupy()
                SeatStatus.AVAILABLE -> seat.available()
                else -> throw ErrorException(ErrorCode.NOT_FOUND_SEAT_STATUS)
            }
        }
    }

    @Transactional
    fun validateBookSeats(seatIds: List<Long>): List<Seat> {
        val selectedSeats = seatRepository.findAllByIdInWithOptimisticLock(seatIds)

        selectedSeats.forEach { seat ->
            seat.validateAvailable()
            seat.book()
        }

        try {
            seatRepository.flush()
        } catch (e: ObjectOptimisticLockingFailureException) {
            throw ErrorException(ErrorCode.SEAT_ALREADY_BOOKED)
        }
        return selectedSeats
    }

    fun getSeatsForPerformance(performanceId: Long): List<BookSeatViewDto> {
        return seatRepository.findValidSeatsByPerformanceId(performanceId)
            .map{ it.toBookSeatViewDto() }
    }


    @Transactional
    fun createSeats(seatList: List<Seat>) {
        val batchSize = 100
        seatList.forEachIndexed { index, seat ->
            entityManager.persist(seat)
            if (index > 0 && index % batchSize == 0) {
                entityManager.flush()
                entityManager.clear()
            }
        }
        entityManager.flush()
        entityManager.clear()
    }

    @Transactional
    fun deleteSeatsByPerformanceId(performanceId: Long) {
        seatRepository.updateSeatStatusByPerformanceId(performanceId, SeatStatus.DELETED)
    }
}
