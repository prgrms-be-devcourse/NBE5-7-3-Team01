package com.fifo.ticketing.domain.book.dto

import lombok.Setter
import java.time.LocalDateTime

data class BookCompleteDto(
    val performanceId: Long,
    val performanceTitle: String,
    val encodedFileName: String,
    val performanceStartTime: LocalDateTime,
    val performanceEndTime: LocalDateTime,
    val placeName: String,
    val seats: List<BookSeatViewDto>,
    val totalPrice: Int,
    val quantity: Int,
    var paymentCompleted: Boolean = false,

    var urlPrefix: String
) {
    val url: String
        get() = "$urlPrefix$encodedFileName"
}