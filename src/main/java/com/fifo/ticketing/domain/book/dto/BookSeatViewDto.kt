package com.fifo.ticketing.domain.book.dto

import com.fifo.ticketing.domain.seat.entity.SeatStatus

data class BookSeatViewDto(
    val seatId: Long,
    val seatNumber: String,
    val grade: String,
    val price: Int = 0,
    val seatStatus: SeatStatus?
) {
}