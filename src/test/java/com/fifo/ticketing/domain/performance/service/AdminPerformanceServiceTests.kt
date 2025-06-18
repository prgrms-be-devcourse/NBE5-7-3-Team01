package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.performance.repository.PlaceRepository
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.event.PerformanceCanceledEvent
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.service.ImageFileService
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions
import org.mockito.*
import org.mockito.kotlin.*
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class AdminPerformanceServiceTests {

    @Mock
    private lateinit var placeRepository: PlaceRepository

    @Mock
    private lateinit var gradeRepository: GradeRepository

    @Mock
    private lateinit var performanceAdminRepository: PerformanceAdminRepository

    @Mock
    private lateinit var performanceRepository: PerformanceRepository

    @Mock
    private lateinit var likeCountRepository: LikeCountRepository

    @Mock
    private lateinit var seatService: SeatService

    @Mock
    private lateinit var imageFileService: ImageFileService

    @Mock
    private lateinit var bookService: BookService

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @InjectMocks
    private lateinit var adminPerformanceService: AdminPerformanceService

    @Mock
    private lateinit var file: MultipartFile

    private lateinit var validator: Validator

    private lateinit var place: Place
    private lateinit var performanceRequestDto: PerformanceRequestDto

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
        place = Place(1L, "서울특별시 서초구 서초동 1307", "강남아트홀", 100)
        performanceRequestDto = PerformanceRequestDto(
            "라따뚜이",
            "라따뚜이는 픽사의 영화입니다.",
            Category.MOVIE,
            false,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            LocalDateTime.of(2025, 5, 12, 19, 0),
            place.id!!
        )
    }

    @Test
    @DisplayName("공연 등록이 성공하는 경우")
    fun `test create performance success`() {
        given(placeRepository.findById(any())).willReturn(Optional.of(place))

        val performance = PerformanceMapper.toEntity(performanceRequestDto, place)
        given(performanceAdminRepository.save(any())).willReturn(performance)

        val uploadedFile = File(null, "encoded-uuid.webp", "default.webp")
        given(imageFileService.uploadFile(file)).willReturn(uploadedFile)

        val grade = Grade(null, place, "S", 20, 20000)
        given(gradeRepository.findAllByPlaceId(any())).willReturn(listOf(grade))

        willDoNothing().given(seatService).createSeats(anyList())

        val likeCountCaptor = ArgumentCaptor.forClass(LikeCount::class.java)
        given(likeCountRepository.save(likeCountCaptor.capture())).willReturn(
            LikeCount(1L, performance, 0L)
        )

        val saved = adminPerformanceService.createPerformance(performanceRequestDto, file)

        assertThat(saved).isNotNull()
        assertThat(saved.title).isEqualTo(performanceRequestDto.title)
        assertThat(saved.description).isEqualTo(performanceRequestDto.description)
        assertThat(saved.place).isEqualTo(place)
        assertThat(saved.file).isEqualTo(uploadedFile)

        val savedLikeCount = likeCountCaptor.value
        assertThat(savedLikeCount.likeCount).isEqualTo(0L)
        assertThat(savedLikeCount.performance).isEqualTo(performance)
    }

    @Test
    @DisplayName("공연 등록 시 장소가 존재하지 않는 경우 예외 처리")
    fun `test create performance not found place`() {
        given(placeRepository.findById(any())).willReturn(Optional.empty())

        assertThatThrownBy {
            adminPerformanceService.createPerformance(performanceRequestDto, file)
        }.isInstanceOf(ErrorException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_PLACES)
    }

    @Test
    @DisplayName("공연 등록 시 파일 업로드에 실패하는 경우 예외 처리")
    fun `test create performance file upload failed`() {
        given(placeRepository.findById(any())).willReturn(Optional.of(place))
        given(performanceAdminRepository.save(any())).willReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place)
        )
        given(imageFileService.uploadFile(file)).willReturn(
            File(null, "encoded-uuid.webp", "default.webp")
        )
        given(gradeRepository.findAllByPlaceId(any())).willReturn(emptyList())

        assertThatThrownBy {
            adminPerformanceService.createPerformance(performanceRequestDto, file)
        }.isInstanceOf(ErrorException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_GRADE)
    }

    @Test
    @DisplayName("공연 등록 시 등급이 존재하지 않는 경우 예외 처리")
    fun `test create performance not found grade`() {
        given(placeRepository.findById(any())).willReturn(Optional.of(place))
        given(performanceAdminRepository.save(any())).willReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place)
        )
        given(imageFileService.uploadFile(file)).willReturn(
            File(null, "encoded-uuid.webp", "default.webp")
        )
        given(gradeRepository.findAllByPlaceId(any())).willReturn(emptyList())

        assertThatThrownBy {
            adminPerformanceService.createPerformance(performanceRequestDto, file)
        }.isInstanceOf(ErrorException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_GRADE)
    }

    @Test
    @DisplayName("공연 등록 시 좌석 생성 중에 실패하는 경우 예외 처리")
    fun `test create performance seat create failed`() {
        given(placeRepository.findById(any())).willReturn(Optional.of(place))
        given(performanceAdminRepository.save(any())).willReturn(
            PerformanceMapper.toEntity(performanceRequestDto, place)
        )
        given(imageFileService.uploadFile(file)).willReturn(
            File(null, "encoded-uuid.webp", "default.webp")
        )
        given(gradeRepository.findAllByPlaceId(any())).willReturn(
            listOf(Grade(1L, place, "S", 10, 10000))
        )

        given(seatService.createSeats(anyList())).willThrow(RuntimeException("Seat create failed"))

        assertThatThrownBy {
            adminPerformanceService.createPerformance(performanceRequestDto, file)
        }.isInstanceOf(ErrorException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEAT_CREATE_FAILED)
    }

    @Test
    @DisplayName("공연 등록 시 공연 시작 시간이 공연 종료 시간보다 이후인 경우 예외 처리")
    fun `test create performance throws exception when startTime is after endTime`() {
        // given
        val request = PerformanceRequestDto(
            title = "Test Performance",
            description = "A great show",
            category = Category.CONCERT,
            performanceStatus = true,
            startTime = LocalDateTime.of(2025, 9, 1, 20, 0),
            endTime = LocalDateTime.of(2025, 9, 1, 19, 0), // 종료시간보다 시작시간이 늦음
            reservationStartTime = LocalDateTime.of(2025, 8, 1, 10, 0),
            placeId = 1L
        )

        // when
        val violations = validator.validate(request)

        // then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message == "INVALID_DATETIME_PERIOD" }).isTrue()
    }

    @Test
    @DisplayName("공연 등록 시 예약 시작 시간이 동연 시작 시간보다 이후인 경우 예외 처리")
    fun `test create performance throws Exception when startTime is after reservationTime`() {
        // given
        val request = PerformanceRequestDto(
            title = "Test Performance",
            description = "A great show",
            category = Category.CONCERT,
            performanceStatus = true,
            startTime = LocalDateTime.of(2025, 9, 1, 17, 0),
            endTime = LocalDateTime.of(2025, 9, 1, 19, 0),
            reservationStartTime = LocalDateTime.of(2025, 9, 2, 10, 0), // 예약 시작 시간보다 공연 시작 시작이 늦음
            placeId = 1L
        )

        // when
        val violations = validator.validate(request)

        // then
        assertThat(violations).isNotEmpty()
        assertThat(violations.any { it.message == "INVALID_DATETIME_RESERVATION" }).isTrue()
    }

    @DisplayName("공연 삭제 성공 - soft delete 플래그만 true로 변경됨")
    @Test
    fun test_deletePerformance_softDelete_success() {
        // Given
        val performanceId = 1L
        val oldPlace = Place(1L, "서울특별시 서초구 서초동 1307", "구 공연장", 100)

        val performance = Performance(
            performanceId, "구 공연 제목",
            "구 공연입니다.",
            oldPlace,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(3),
            Category.MOVIE,
            false,
            false,
            LocalDateTime.now().minusDays(3),
            File(
                10L,
                "encoded_001.jpg",
                "001.jpg"
            )
        )

        Mockito.`when`(performanceAdminRepository!!.findByIdAndDeletedFlagFalse(performanceId))
            .thenReturn(Optional.of(performance))

        // 예약 취소 → 빈 리스트 반환
        Mockito.`when`(
            bookService!!.cancelAllBook(performance)
        ).thenReturn(emptyList())

        // 좌석 삭제, 이벤트 발행 → 아무 동작 안 함
        Mockito.doNothing().`when`(seatService)?.deleteSeatsByPerformanceId(performanceId)
        Mockito.doNothing().`when`(eventPublisher)?.publishEvent(
            ArgumentMatchers.any(
                PerformanceCanceledEvent::class.java
            )
        )

        // When
        adminPerformanceService!!.deletePerformance(performanceId)

        // Then
        Mockito.verify(performanceAdminRepository).flush() // flush가 호출되었는지
        Assertions.assertTrue(performance.deletedFlag, "soft delete 플래그가 true로 설정되어야 합니다.")

        // 추가적으로 필요한 동작 검증
        Mockito.verify(bookService).cancelAllBook(performance)
        Mockito.verify(seatService)?.deleteSeatsByPerformanceId(performanceId)
        Mockito.verify(eventPublisher)?.publishEvent(
            ArgumentMatchers.any(
                PerformanceCanceledEvent::class.java
            )
        )
    }

    @DisplayName("삭제하려는 공연이 존재하지 않으면 예외 발생")
    @Test
    fun test_deletePerformance_notFound_throwsError() {
        // Given
        val invalidId = 999L
        Mockito.`when`(performanceAdminRepository!!.findByIdAndDeletedFlagFalse(invalidId))
            .thenReturn(Optional.empty())

        // When & Then
        val exception = Assertions.assertThrows(
            ErrorException::class.java
        ) {
            adminPerformanceService!!.deletePerformance(invalidId)
        }

        org.assertj.core.api.Assertions.assertThat(exception.errorCode)
            .isEqualTo(ErrorCode.NOT_FOUND_PERFORMANCE)
    }
}