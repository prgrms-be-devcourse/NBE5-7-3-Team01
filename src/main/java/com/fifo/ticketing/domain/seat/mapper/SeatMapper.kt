package com.fifo.ticketing.domain.seat.mapper

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
import com.fifo.ticketing.domain.seat.entity.Seat

object SeatMapper {
    fun toBookSeatViewDto(seat: Seat): BookSeatViewDto {
        val grade = seat.grade

        return BookSeatViewDto(
            TODO("Cannot convert element")
        ).seatId(seat.id)
            .seatNumber(seat.seatNumber)
            .grade(grade.grade)
            .price(seat.price)
            .seatStatus(seat.seatStatus)
    }
}
