package com.fifo.ticketing.domain.book.dto

import com.fifo.ticketing.domain.book.entity.BookStatus

data class BookedView(
    val bookId: Long,
    val performanceId: Long,
    val performanceTitle: String,
    val encodedFileName: String,
    val placeName: String,
    val seats: List<BookSeatViewDto>,
    val totalPrice: Int = 0,
    val quantity: Int = 0,
    val bookStatus: BookStatus,
    val urlPrefix: String,

    ) {
    fun getUrl(): String = "$urlPrefix$encodedFileName"
}
