package com.fifo.ticketing.domain.book.dto

import jakarta.validation.constraints.NotBlank

data class BookCreateRequest(

    @field: NotBlank
    val seatIds: MutableList<Long>

)
