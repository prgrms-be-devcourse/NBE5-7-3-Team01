package com.fifo.ticketing.domain.performance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fifo.ticketing.domain.book.service.BookService;
import com.fifo.ticketing.domain.like.entity.LikeCount;
import com.fifo.ticketing.domain.like.repository.LikeCountRepository;
import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto;
import com.fifo.ticketing.domain.performance.entity.Category;
import com.fifo.ticketing.domain.performance.entity.Grade;
import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.performance.entity.Place;
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper;
import com.fifo.ticketing.domain.performance.repository.GradeRepository;
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository;
import com.fifo.ticketing.domain.performance.repository.PlaceRepository;
import com.fifo.ticketing.domain.seat.service.SeatService;
import com.fifo.ticketing.global.entity.File;
import com.fifo.ticketing.global.exception.ErrorCode;
import com.fifo.ticketing.global.exception.ErrorException;
import com.fifo.ticketing.global.service.ImageFileService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@ActiveProfiles("ci")
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTests {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private PerformanceAdminRepository performanceAdminRepository;

    @Mock
    private LikeCountRepository likeCountRepository;

    @Mock
    private SeatService seatService;

    @Mock
    private BookService bookService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ImageFileService imageFileService;

    @InjectMocks
    private AdminPerformanceService adminPerformanceService;

    @Mock
    private MultipartFile file;

    private Place place;
    private PerformanceRequestDto performanceRequestDto;

    @BeforeEach
    void setUp() {

        place = new Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100);

        performanceRequestDto = new PerformanceRequestDto(
            "라따뚜이",
            "라따뚜이는 픽사의 영화입니다.",
            Category.MOVIE,
            false,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            LocalDateTime.of(2025, 5, 12, 19, 0),
            place.getId()
        );
    }

    @Test
    @DisplayName("공연 등록이 성공하는 경우")
    void test_create_performance_success() throws Exception {
        // Given
        when(placeRepository.findById(any(Long.class))).thenReturn(Optional.of(place));

        Performance performance = PerformanceMapper.toEntity(performanceRequestDto, place);
        when(performanceAdminRepository.save(any(Performance.class))).thenReturn(performance);

        File uploadedFile = new File(null, "encoded-uuid.webp", "default.webp");
        when(imageFileService.uploadFile(file)).thenReturn(uploadedFile);

        Grade grade = new Grade(null, place, "S", 20, 20000);
        when(gradeRepository.findAllByPlaceId(any(Long.class))).thenReturn(List.of(grade));

        doNothing().when(seatService).createSeats(anyList());

        ArgumentCaptor<LikeCount> likeCountCaptor = ArgumentCaptor.forClass(LikeCount.class);
        when(likeCountRepository.save(likeCountCaptor.capture())).thenReturn(
            LikeCount.builder().id(1L).likeCount(0L).performance(performance).build());

        // When
        Performance savePerformance = adminPerformanceService.createPerformance(
            performanceRequestDto, file);

        // Then
        assertThat(savePerformance).isNotNull();
        assertThat(savePerformance.getTitle()).isEqualTo(performanceRequestDto.getTitle());
        assertThat(savePerformance.getDescription()).isEqualTo(
            performanceRequestDto.getDescription());
        assertThat(savePerformance.getPlace()).isEqualTo(place);
        assertThat(savePerformance.getFile()).isEqualTo(uploadedFile);
        LikeCount savedLikeCount = likeCountCaptor.getValue();
        assertThat(savedLikeCount.getLikeCount()).isEqualTo(0L);
        assertThat(savedLikeCount.getPerformance()).isEqualTo(performance);

        // Verify
        verify(placeRepository).findById(any(Long.class));
        verify(performanceAdminRepository).save(any(Performance.class));
        verify(imageFileService).uploadFile(file);
        verify(gradeRepository).findAllByPlaceId(any(Long.class));
        verify(seatService).createSeats(anyList());
        verify(likeCountRepository).save(any(LikeCount.class));
    }

    @Test
    @DisplayName("공연 등록 시 장소가 존재하지 않는 경우 예외 처리")
    void test_create_performance_not_found_place() {
        // Given
        when(placeRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(
            () -> adminPerformanceService.createPerformance(performanceRequestDto, file))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PLACES);

        // Verify
        verify(placeRepository).findById(any(Long.class));
        verifyNoInteractions(performanceAdminRepository, imageFileService, gradeRepository,
            seatService);
    }

    @Test
    @DisplayName("공연 등록 시 파일 업로드에 실패하는 경우 예외 처리")
    void test_create_performance_file_upload_failed() throws Exception {
        // Given
        when(placeRepository.findById(any(Long.class))).thenReturn(Optional.of(place));
        when(performanceAdminRepository.save(any(Performance.class))).thenReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place));
        when(imageFileService.uploadFile(file)).thenThrow(new IOException("파일 업로드 실패"));

        // When & Then
        assertThatThrownBy(
            () -> adminPerformanceService.createPerformance(performanceRequestDto, file))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAILED);

        // Verify : 파일 업로드는 호출, 좌석 생성은 호출되지 않습니다!
        verify(imageFileService).uploadFile(file);
        verifyNoMoreInteractions(seatService, gradeRepository);
    }

    @Test
    @DisplayName("공연 등록 시 등급이 존재하지 않는 경우 예외 처리")
    void test_create_performance_not_found_grade() throws Exception {
        // Given
        when(placeRepository.findById(any(Long.class))).thenReturn(Optional.of(place));
        when(performanceAdminRepository.save(any(Performance.class))).thenReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place));
        when(imageFileService.uploadFile(file)).thenReturn(
            new File(null, "encoded-uuid.webp", "default.webp"));
        when(gradeRepository.findAllByPlaceId(any(Long.class))).thenReturn(List.of());

        // When & Then
        assertThatThrownBy(
            () -> adminPerformanceService.createPerformance(performanceRequestDto, file))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_GRADE);

        // Verify : 등급 조회는 호출, 좌석 생성은 호출되지 않습니다!
        verify(gradeRepository).findAllByPlaceId(place.getId());
        verifyNoInteractions(seatService);
    }

    @Test
    @DisplayName("공연 등록 시 좌석 생성 중에 실패하는 경우 예외 처리")
    void test_create_performance_seat_create_failed() throws Exception {

        // Given
        when(placeRepository.findById(any(Long.class))).thenReturn(Optional.of(place));
        when(performanceAdminRepository.save(any(Performance.class))).thenReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place));
        when(imageFileService.uploadFile(file)).thenReturn(
            new File(null, "encoded-uuid.webp", "default.webp"));
        when(gradeRepository.findAllByPlaceId(any(Long.class))).thenReturn(
            List.of(new Grade(1L, place, "S", 10, 10000)));
        doThrow(new RuntimeException("Seat create failed")).when(seatService)
            .createSeats(anyList());

        // When & Then
        assertThatThrownBy(
            () -> adminPerformanceService.createPerformance(performanceRequestDto, file))
            .isInstanceOf(ErrorException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_CREATE_FAILED);

        verify(seatService).createSeats(anyList());
        verifyNoMoreInteractions(seatService);
    }

    @Test
    @DisplayName("공연 수정이 성공하는 경우")
    void test_update_performance_success() throws Exception {

        // Given
        Long performanceId = 1L;
        Long newPlaceId = 3L;

        Place oldPlace = new Place(2L, "서울특별시 서초구 서초동 1307", "구 공연장", 100);
        Place newPlace = new Place(3L, "서울특별시 서초구 서초동 1307", "신 공연장", 100);

        Performance performance = new Performance
            (performanceId, "구 공연 제목",
                "구 공연입니다.",
                oldPlace,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                Category.MOVIE,
                false,
                false,
                LocalDateTime.now().minusDays(3),
                new File(
                    10L,
                    "encoded_oldFile.jpg",
                    "oldFile.jpg"
                )
            );

        List<Grade> grades = List.of(new Grade(1L, newPlace, "S", 900000, 50));

        MultipartFile newFile = new MockMultipartFile("file", "new.jpg", "image/jpeg",
            "new image".getBytes());

        PerformanceRequestDto requestDto = new PerformanceRequestDto(
            "신 공연 제목", "신 공연입니다.",
            Category.CONCERT, false,
            LocalDateTime.now().plusHours(5),
            LocalDateTime.now().plusHours(8),
            LocalDateTime.now().minusDays(2),
            newPlaceId
        );

        // When
        given(performanceAdminRepository.findById(performanceId)).willReturn(
            Optional.of(performance));
        given(placeRepository.findById(newPlaceId)).willReturn(Optional.of(newPlace));
        given(gradeRepository.findAllByPlaceId(newPlaceId)).willReturn(grades);

        given(bookService.cancelAllBook(performance)).willReturn(List.of());

        // 기존 파일이 있는 경우 -> updateFile
        willDoNothing().given(imageFileService).updateFile(any(), any());

        // Then
        Performance updated = adminPerformanceService.updatePerformance(performanceId, requestDto,
            newFile);

        // Verify
        verify(seatService).deleteSeatsByPerformanceId(performanceId);
        verify(seatService).createSeats(anyList());
        verify(imageFileService).updateFile(any(), eq(newFile));
        assertEquals("신 공연 제목", updated.getTitle());
        assertEquals(newPlace, updated.getPlace());
    }

    @Test
    @DisplayName("공연 수정 파일만 변경하는 경우")
    void test_update_performance_file_success() throws Exception {

        // Given
        Long performanceId = 1L;
        Place oldPlace = new Place(1L, "서울특별시 서초구 서초동 1307", "구 공연장", 100);
        Performance performance = new Performance
            (performanceId, "구 공연 제목",
                "구 공연입니다.",
                oldPlace,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3),
                Category.MOVIE,
                false,
                false,
                LocalDateTime.now().minusDays(3),
                new File(
                    10L,
                    "encoded_001.jpg",
                    "001.jpg"
                )
            );

        PerformanceRequestDto requestDto = new PerformanceRequestDto(
            "구 공연 제목", "구 공연입니다.",
            Category.MOVIE, false,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(3),
            LocalDateTime.now().minusDays(3),
            oldPlace.getId()
        );

        MultipartFile newFile = new MockMultipartFile(
            "encodedFile",
            "001.png",
            "image/png",
            Files.readAllBytes(Path.of("src/test/resources/uploads/001.png"))  // 실제 이미지 파일 바이트
        );

        File uploadedFile = new File(
            10L,
            "encoded_001.jpg",
            "001.jpg"
        );

        given(performanceAdminRepository.findById(performanceId)).willReturn(
            Optional.of(performance));
        given(placeRepository.findById(1L)).willReturn(Optional.of(oldPlace));
        doNothing().when(imageFileService).updateFile(any(), eq(newFile));

        // When
        Performance updated = adminPerformanceService.updatePerformance(performanceId, requestDto,
            newFile);

        // Verify
        verify(imageFileService).updateFile(any(), eq(newFile));
        assertEquals("구 공연 제목", updated.getTitle());
        assertEquals(oldPlace, updated.getPlace());
        assertEquals(uploadedFile.getId(), updated.getFile().getId());

    }
}