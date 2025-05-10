package com.fifo.ticketing.domain.seat.mapper;

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto;
import com.fifo.ticketing.domain.seat.entity.Seat;

public class SeatMapper {

    public static BookSeatViewDto toBookSeatViewDto(Seat seat) {
        return BookSeatViewDto.builder()
            .seatId(seat.getId())
            .seatNumber(seat.getSeatNumber())
            .grade(seat.getGrade())
            .price(seat.getPrice())
            .seatStatus(seat.getSeatStatus())
            .build();
    }
}
