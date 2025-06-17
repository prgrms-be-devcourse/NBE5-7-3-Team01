package com.fifo.ticketing.domain.performance.service;

import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.seat.repository.SeatRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformanceReservationOpenService {

    private final PerformanceRepository performanceRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public void updateStatusIfReservationStart() {
        performanceRepository.updatePerformanceStatusToReservationStart(LocalDateTime.now());
    }

    @Transactional
    public void updateStatusIfSoldOutOrCanceled() {
        performanceRepository.findActivePerformances((LocalDateTime.now())).forEach(performance -> {
            Long performanceId = performance.getId();
            int availableSeats = seatRepository.countAvailableSeatsByPerformanceId(performanceId);

            if (availableSeats == 0) {
                performanceRepository.updatePerformanceStatusReservationUnavailable(performanceId);
            } else if (availableSeats > 0) {
                performanceRepository.updatePerformanceStatusReservationAvailable(performanceId);
            }
        });
    }
}
