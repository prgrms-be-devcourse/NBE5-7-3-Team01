package com.fifo.ticketing.domain.book.dto

import com.fifo.ticketing.domain.book.entity.BookStatus
import java.time.LocalDateTime

data class BookMailSendDto(
    val emailAddr: String,
    val title: String,
    val performanceId: Long,
    val performanceTitle: String,
    val performanceStartTime: LocalDateTime,
    val performanceEndTime: LocalDateTime,
    val placeName: String,
    val seats: List<BookSeatViewDto>,
    val totalPrice: Int = 0,
    val quantity: Int = 0,
    val bookStatus: BookStatus,
)
