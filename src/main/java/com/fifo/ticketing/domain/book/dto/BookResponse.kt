package com.fifo.ticketing.domain.book.dto

data class BookResponse(

    val bookId: Long,
    val totalPrice: Int = 0,
    val quantity: Int = 0

)
