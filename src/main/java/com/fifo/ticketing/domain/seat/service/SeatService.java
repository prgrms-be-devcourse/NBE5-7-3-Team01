package com.fifo.ticketing.domain.seat.service;

import com.fifo.ticketing.domain.book.dto.BookSeatViewDto;
import com.fifo.ticketing.domain.seat.dto.SeatMapper;
import com.fifo.ticketing.domain.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public List<BookSeatViewDto> getSeatsForPerformance(Long performanceId) {
        return seatRepository.findAllByPerformanceId(performanceId)
            .stream()
            .map(SeatMapper::toBookSeatViewDto)
            .collect(Collectors.toList());
    }
}
