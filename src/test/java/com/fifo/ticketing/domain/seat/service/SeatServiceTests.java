package com.fifo.ticketing.domain.seat.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Grade;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.seat.entity.Seat;
import com.fifo.ticketing.domain.seat.entity.SeatStatus;
import com.fifo.ticketing.domain.seat.repository.SeatRepository;
import com.fifo.ticketing.global.entity.File;
import com.fifo.ticketing.global.exception.AlertDetailException;
import com.fifo.ticketing.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("ci")
@ExtendWith(MockitoExtension.class)
class SeatServiceTests {

    @InjectMocks
    private SeatService seatService;

    @Mock
    private SeatRepository seatRepository;

    private Place place;
    private Performance mockPerformance;
    private Grade modckGrade;


    @BeforeEach
    void setUp() {

        place = new Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100);
        File mockFile = File.builder()
            .id(1L)
            .encodedFileName("poster.jpg")
            .originalFileName("sample.jpg")
            .build();

        mockPerformance = Performance.builder()
            .id(1L)
            .title("라따뚜이")
            .description("라따뚜이는 픽사의 영화입니다.")
            .place(place)
            .startTime(LocalDateTime.of(2025, 6, 1, 19, 0))
            .endTime(LocalDateTime.of(2025, 6, 1, 21, 0))
            .category(Category.MOVIE)
            .performanceStatus(false)
            .deletedFlag(false)
            .reservationStartTime(LocalDateTime.of(2025, 5, 12, 19, 0))
            .file(mockFile)
            .build();

        modckGrade = Grade.builder()
            .id(1L)
            .grade("A")
            .place(place)
            .defaultPrice(5000)
            .seatCount(10)
            .build();


    }

    @Test
    @DisplayName("validateBookSeats - 좌석이 AVAILABLE 상태면 BOOKED 상태로 바꿔서 반환한다 ")
    void validateBookSeats_success() {

        List<Long> seatIds = List.of(1L, 2L, 3L);

        Seat mockSeat1 = new Seat(1L, mockPerformance, "A1", 5000, modckGrade,
            SeatStatus.AVAILABLE);
        Seat mockSeat2 = new Seat(2L, mockPerformance, "A1", 5000, modckGrade,
            SeatStatus.AVAILABLE);
        Seat mockSeat3 = new Seat(3L, mockPerformance, "A1", 5000, modckGrade,
            SeatStatus.AVAILABLE);

        List<Seat> mockSeats = List.of(mockSeat1, mockSeat2, mockSeat3);

        given(seatRepository.findAllByIdInWithOptimisticLock(seatIds))
            .willReturn(mockSeats);

        List<Seat> resultSeats = seatService.validateBookSeats(seatIds);

        assertEquals(mockSeat1.getId(), resultSeats.get(0).getId());
        assertEquals(mockSeat2.getId(), resultSeats.get(1).getId());
        assertEquals(mockSeat3.getId(), resultSeats.get(2).getId());

        assertEquals(resultSeats.get(0).getSeatStatus(), SeatStatus.BOOKED);
        assertEquals(resultSeats.get(1).getSeatStatus(), SeatStatus.BOOKED);
        assertEquals(resultSeats.get(2).getSeatStatus(), SeatStatus.BOOKED);

    }


    @Test
    @DisplayName("validateBookSeats - 좌석이 이미 BOOKED 상태면 AlertDetailException이 터진다")
    void validateBookSeats_Exception_success() {

        List<Long> seatIds = List.of(1L, 2L);

        Seat mockSeat1 = new Seat(1L, mockPerformance, "A1", 5000, modckGrade,
            SeatStatus.AVAILABLE);
        Seat mockSeat2 = new Seat(2L, mockPerformance, "A1", 5000, modckGrade, SeatStatus.BOOKED);

        List<Seat> mockSeats = List.of(mockSeat1, mockSeat2);

        given(seatRepository.findAllByIdInWithOptimisticLock(seatIds))
            .willReturn(mockSeats);

        AlertDetailException exception = assertThrows(AlertDetailException.class,
            () -> seatService.validateBookSeats(seatIds));

        assertTrue(exception.getMessage().contains("2번 좌석은 이미 예약되었습니다."));
        assertEquals(ErrorCode.SEAT_ALREADY_BOOKED, exception.getErrorCode());
    }
}

