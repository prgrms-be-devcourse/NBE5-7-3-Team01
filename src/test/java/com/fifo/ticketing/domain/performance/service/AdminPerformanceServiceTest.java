package com.fifo.ticketing.domain.performance.service;

import com.fifo.ticketing.domain.book.repository.BookRepository;
import com.fifo.ticketing.domain.book.service.BookService;
import com.fifo.ticketing.domain.like.repository.LikeCountRepository;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository;
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository;
import com.fifo.ticketing.domain.performance.repository.PlaceRepository;
import com.fifo.ticketing.domain.seat.service.SeatService;
import com.fifo.ticketing.global.entity.File;
import com.fifo.ticketing.global.event.PerformanceCanceledEvent;
import com.fifo.ticketing.global.exception.ErrorException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;

import static com.fifo.ticketing.global.exception.ErrorCode.NOT_FOUND_PERFORMANCE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminPerformanceServiceUnitTest {

    @InjectMocks
    private AdminPerformanceService adminPerformanceService;

    @Mock private PerformanceRepository performanceRepository;
    @Mock private PerformanceAdminRepository performanceAdminRepository;
    @Mock private PlaceRepository placeRepository;
    @Mock private LikeCountRepository likeCountRepository;
    @Mock private BookRepository bookRepository;
    @Mock private SeatService seatService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("공연 삭제 성공 - soft delete 플래그만 true로 변경됨")
    @Test
    void test_deletePerformance_softDelete_success() {
        // Given
        Long performanceId = 1L;
        Place oldPlace = new Place(1L, "서울특별시 서초구 서초동 1307", "구 공연장", 100);

        Performance performance = new Performance (
                performanceId, "구 공연 제목",
                "구 공연입니다.",
                oldPlace,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                Category.MOVIE,
                false,
                false,
                LocalDateTime.now().minusDays(3),
                File.builder().id(10L).originalFileName("001.jpg").build()
        );

        when(performanceAdminRepository.findByIdAndDeletedFlagFalse(performanceId))
            .thenReturn(Optional.of(performance));

        // 예약 취소 → 빈 리스트 반환
        when(bookService.cancelAllBook(performance)).thenReturn(Collections.emptyList());

        // 좌석 삭제, 이벤트 발행 → 아무 동작 안 함
        doNothing().when(seatService).deleteSeatsByPerformanceId(performanceId);
        doNothing().when(eventPublisher).publishEvent(any(PerformanceCanceledEvent.class));

        // When
        adminPerformanceService.deletePerformance(performanceId);

        // Then
        verify(performanceAdminRepository).flush(); // flush가 호출되었는지
        assertTrue(performance.getDeletedFlag(), "soft delete 플래그가 true로 설정되어야 합니다.");

        // 추가적으로 필요한 동작 검증
        verify(bookService).cancelAllBook(performance);
        verify(seatService).deleteSeatsByPerformanceId(performanceId);
        verify(eventPublisher).publishEvent(any(PerformanceCanceledEvent.class));
    }

    @DisplayName("삭제하려는 공연이 존재하지 않으면 예외 발생")
    @Test
    void test_deletePerformance_notFound_throwsError() {
        // Given
        Long invalidId = 999L;
        when(performanceAdminRepository.findByIdAndDeletedFlagFalse(invalidId))
            .thenReturn(Optional.empty());

        // When & Then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            adminPerformanceService.deletePerformance(invalidId);
        });

        assertThat(exception.getErrorCode()).isEqualTo(NOT_FOUND_PERFORMANCE);
    }
}
