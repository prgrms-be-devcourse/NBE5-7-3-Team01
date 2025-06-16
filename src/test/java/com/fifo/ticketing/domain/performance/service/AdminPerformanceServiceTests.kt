package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository
import com.fifo.ticketing.domain.performance.repository.PlaceRepository
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.service.ImageFileService
import jakarta.validation.Validation
import jakarta.validation.Validator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.mockito.kotlin.*
import org.mockito.ArgumentMatchers.anyList
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.InjectMocks
import org.mockito.Mock
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
    private lateinit var likeCountRepository: LikeCountRepository

    @Mock
    private lateinit var seatService: SeatService

    @Mock
    private lateinit var bookService: BookService

    @Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var imageFileService: ImageFileService

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
}