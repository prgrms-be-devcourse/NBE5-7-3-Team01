package com.fifo.ticketing.domain.book.dto

import com.fifo.ticketing.domain.book.entity.BookStatus
import lombok.Setter

class BookUserDetailDto constructor(
    val bookId: Long,
    val performanceId: Long,
    val title: String,
    val totalPrice: Int,
    val quantity: Int,
    val username: String,
    val encodedFileName: String,
    val bookStatus: BookStatus,

    ) {
    var urlPrefix: String = ""

    fun getUrl(): String = "$urlPrefix$encodedFileName"
}
