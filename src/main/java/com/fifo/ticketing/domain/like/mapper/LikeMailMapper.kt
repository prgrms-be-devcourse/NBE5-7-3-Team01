package com.fifo.ticketing.domain.like.mapper

import com.fifo.ticketing.domain.like.dto.NoPayedMailDto
import com.fifo.ticketing.domain.like.dto.ReservationStartMailDto
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User

object LikeMailMapper {
    @JvmStatic
    fun toReservationStartMailDto(user: User, performance: Performance): ReservationStartMailDto {
        return ReservationStartMailDto(
            email= user.getEmail(),
            username = user.getUsername(),
            performanceTitle = performance.getTitle(),
            reservationStartTime =performance.getReservationStartTime()
        )
    }

    @JvmStatic
    fun toNoPayedMailDto(user: User, performance: Performance, availableSeats: Int): NoPayedMailDto {
        return NoPayedMailDto(
            email = user.getEmail(),
            username = user.getUsername(),
            performanceTitle = performance.getTitle(),
            availableSeats = availableSeats,
        )
    }

}
