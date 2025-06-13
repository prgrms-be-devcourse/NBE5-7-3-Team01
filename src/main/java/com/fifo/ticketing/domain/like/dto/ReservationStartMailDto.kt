package com.fifo.ticketing.domain.like.dto


import java.time.LocalDateTime

class ReservationStartMailDto(
    val email: String,
    val username: String,
    val performanceTitle: String,
    val reservationStartTime: LocalDateTime,
)