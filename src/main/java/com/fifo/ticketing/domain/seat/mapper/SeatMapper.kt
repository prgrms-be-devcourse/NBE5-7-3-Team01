package com.fifo.ticketing.domain.seat.mapper

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
import com.fifo.ticketing.domain.seat.entity.Seat

object SeatMapper {
    @JvmStatic
    fun toBookSeatViewDto(seat: Seat): BookSeatViewDto {
        val grade = seat.grade

        return BookSeatViewDto(
            seatId = (seat.getId()),
            seatNumber = (seat.getSeatNumber()),
            grade = (grade.getGrade()),
            price = (seat.getPrice()),
            seatStatus = (seat.getSeatStatus()),
        )
    }
}
