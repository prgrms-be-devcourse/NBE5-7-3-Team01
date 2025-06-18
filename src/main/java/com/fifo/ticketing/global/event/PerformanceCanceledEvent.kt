package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.book.entity.Book

data class PerformanceCanceledEvent(
    val canceledBooks: List<Book>
)
