package com.fifo.ticketing.domain.seat.mapper

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
import com.fifo.ticketing.domain.seat.entity.Seat

fun Seat.toBookSeatViewDto(): BookSeatViewDto {
    val grade = this.grade

    return BookSeatViewDto(
        seatId = (this.id!!),
        seatNumber = (this.seatNumber),
        grade = (grade.grade),
        price = (this.price),
        seatStatus = (this.seatStatus),
    )
}

