package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.domain.like.entity.LikeCount
import com.fifo.ticketing.domain.like.repository.LikeCountRepository
import com.fifo.ticketing.domain.performance.dto.*
import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.performance.mapper.PerformanceAdminMapper.toAdminDetailResponseDto
import com.fifo.ticketing.domain.performance.mapper.PerformanceAdminMapper.toAdminPerformanceResponseDto
import com.fifo.ticketing.domain.performance.mapper.PerformanceAdminMapper.toPageAdminPerformanceResponseDto
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper.toEntity
import com.fifo.ticketing.domain.performance.mapper.PerformanceMapper.toSeatGradeDto
import com.fifo.ticketing.domain.performance.mapper.PlaceMapper.toDtoForPerformanceCreate
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceAdminRepository
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.performance.repository.PlaceRepository
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.global.entity.File
import com.fifo.ticketing.global.event.PerformanceCanceledEvent
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import com.fifo.ticketing.global.service.ImageFileService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime

@Service
class AdminPerformanceService(
    private val placeRepository: PlaceRepository,
    private val performanceRepository: PerformanceRepository,
    private val performanceAdminRepository: PerformanceAdminRepository,
    private val gradeRepository: GradeRepository,
    private val seatService: SeatService,
    private val imageFileService: ImageFileService,
    private val likeCountRepository: LikeCountRepository,
    private val bookService: BookService,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Value("\${file.url-prefix}")
    private val urlPrefix: String? = null

    @Transactional(readOnly = true)
    fun getPerformanceDetailForAdmin(performanceId: Long): AdminPerformanceDetailResponse {
        val performance = performanceAdminRepository.findById(performanceId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }
        val grades = gradeRepository.findAllByPlaceId(performance.place.id!!)
        val seatGrades = grades.stream()
            .map<PerformanceSeatGradeDto> { grade -> toSeatGradeDto(grade) }
            .toList()

        return toAdminDetailResponseDto(performance, seatGrades, urlPrefix!!)
    }

    fun getPerformanceUpdateForAdmin(performanceId: Long): AdminPerformanceResponseDto {
        val performance = performanceAdminRepository.findById(performanceId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }
        return toAdminPerformanceResponseDto(performance, urlPrefix!!)
    }

    @Transactional(readOnly = true)
    fun getPerformancesSortedByLatestForAdmin(
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        val performances = performanceAdminRepository
            .findUpcomingPerformancesOrderByReservationStartTimeForAdmin(pageable)
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }

    @Transactional(readOnly = true)
    fun searchPerformancesByKeyword(
        keyword: String?,
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        if (keyword.isNullOrEmpty()) {
            getPerformancesSortedByLatestForAdmin(pageable)
        }
        val performances =
            performanceAdminRepository.findUpcomingPerformancesByKeywordContainingForAdmin(
                LocalDateTime.now(), keyword!!, pageable
            )
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }


    @Transactional
    @Throws(IOException::class)
    fun createPerformance(dto: PerformanceRequestDto, file: MultipartFile): Performance {
        // Place 조회 및 존재여부 확인
        val place = findPlace(dto.placeId)
        // Performance 생성 및 DB 저장
        val savedPerformance = savePerformance(dto, place)
        // File 업로드
        val uploadFile = uploadFile(file)
        // performance의 File을 Update
        savedPerformance.file = uploadFile
        // Grade 조회
        val grades = findGradesByPlace(place.id!!)
        // Seat 목록 생성
        val allSeats = generateSeatsForGrades(grades, savedPerformance)
        // Seats 저장 (Batch) - 100개 단위
        saveSeatsInBatch(allSeats)
        // LikeCount 저장
        saveLikeCount(savedPerformance)
        return savedPerformance
    }

    @Transactional
    @Throws(IOException::class)
    fun updatePerformance(id: Long, dto: PerformanceRequestDto, file: MultipartFile?): Performance {
        // 1. 수정을 위한 Performance 조회.
        val findPerformance = performanceAdminRepository.findById(id).orElseThrow {
            ErrorException(
                ErrorCode.NOT_FOUND_PERFORMANCE
            )
        }

        // 추가. 삭제된 공연에 대해서 예외처리
        deletedPerformanceCheck(findPerformance)

        // 2. Place 조회
        val newPlace = findPlace(dto.placeId)

        // 3. 동일 장소인지 확인 후 수정 및 삭제
        if (findPerformance.place.id != dto.placeId) {
            // 좌석이 삭제되기 때문에 예약을 먼저 전부 취소하고, 메일도 전송해야 합니다.
            val books = bookService.cancelAllBook(findPerformance)
            // 해당 이벤트 자체는 Transaction의 커밋 이후에 이루어집니다.
            eventPublisher.publishEvent(PerformanceCanceledEvent(books))

            // 기존 좌석 삭제 (soft or hard) -> 일단 soft라는 인식
            seatService.deleteSeatsByPerformanceId(id)

            // 새로운 장소 기준 등급 조회 및 좌석 재생성
            val newGrades = findGradesByPlace(newPlace.id!!)
            val newSeats = generateSeatsForGrades(newGrades, findPerformance)
            saveSeatsInBatch(newSeats)
        }

        // 4. 공연 정보 수정
        findPerformance.update(dto, newPlace)

        // 5. 신규 파일이 업로드된 경우
        if (file != null && !file.isEmpty) {
            // 기존 파일 정보가 있는 경우 파일 삭제
            val existFile = findPerformance.file
            if (existFile != null) {
                imageFileService.updateFile(existFile, file)
            } else {
                // 5. 기존 파일 정보가 없는 경우에 새로 업로드된 경우
                val newFile = imageFileService.uploadFile(file)
                findPerformance.file = newFile
            }
        }
        return findPerformance
    }

    @Transactional
    fun deletePerformance(id: Long) {
        // 1. 삭제를 위한 Performance 조회 (삭제되지 않은 파일만)
        val findPerformance = performanceAdminRepository.findByIdAndDeletedFlagFalse(id)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE) }

        // 추가. 삭제된 공연에 대해서 예외처리
        deletedPerformanceCheck(findPerformance)

        // 2. 공연 삭제
        // 예약 삭제 / 좌석 삭제에서 영속성 컨텍스트가 초기화 되고, findPerformance가 flush 되지 않고 detach되는 문제 때문에 flush를 호출
        findPerformance.delete()
        performanceAdminRepository.flush()

        // 3. 예약 삭제
        // books를 변수로 가져온 이유는, books의 유저를 기반으로 메일을 전송하기 위해서입니다.
        val books = bookService.cancelAllBook(findPerformance)

        // 4. 좌석 삭제
        // 좌석 삭제 시에 Query로 처리하는 부분 때문에 flush가 됩니다.
        seatService.deleteSeatsByPerformanceId(id)

        // 후속 절차로 메일 전송을 EventListener로 보낼 예정입니다.
        // 사용 변수는 books 입니다.
        eventPublisher.publishEvent(PerformanceCanceledEvent(books))
    }

    private fun deletedPerformanceCheck(findPerformance: Performance) {
        if (findPerformance.deletedFlag) {
            throw ErrorException(ErrorCode.INVALID_DELETED_PERFORMANCE)
        }
    }

    private fun saveLikeCount(savedPerformance: Performance) {
        likeCountRepository.save(LikeCount(null, savedPerformance, 0L))
    }

    private fun findPlace(placeId: Long): Place {
        return placeRepository.findById(placeId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_PLACES) }
    }

    private fun savePerformance(dto: PerformanceRequestDto, place: Place): Performance {
        val performance = toEntity(dto, place)
        return performanceAdminRepository.save(performance)
    }

    private fun uploadFile(file: MultipartFile): File {
        try {
            return imageFileService.uploadFile(file)
        } catch (e: IOException) {
            throw ErrorException(ErrorCode.FILE_UPLOAD_FAILED)
        }
    }

    private fun findGradesByPlace(placeId: Long): List<Grade> {
        val grades = gradeRepository.findAllByPlaceId(placeId)
        if (grades.isEmpty()) {
            throw ErrorException(ErrorCode.NOT_FOUND_GRADE)
        }
        return grades
    }

    private fun generateSeatsForGrades(grades: List<Grade>, performance: Performance): List<Seat> {
        val allSeats: MutableList<Seat> = ArrayList()
        for (grade in grades) {
            for (seatNumber in 1..grade.seatCount) {
                allSeats.add(Seat.of(performance, grade, seatNumber))
            }
        }
        return allSeats
    }

    private fun saveSeatsInBatch(allSeats: List<Seat>) {
        try {
            seatService.createSeats(allSeats)
        } catch (e: RuntimeException) {
            throw ErrorException(ErrorCode.SEAT_CREATE_FAILED)
        }
    }

    @get:Transactional(readOnly = true)
    val allPlaces: List<PlaceResponseDto>
        get() {
            val places = placeRepository.findAll()
            return places.stream()
                .map<PlaceResponseDto> { place -> toDtoForPerformanceCreate(place) }
                .toList()
        }

    @Transactional(readOnly = true)
    fun getPerformancesSortedByLikesForAdmin(
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        val performances =
            performanceAdminRepository.findUpcomingPerformancesOrderByLikesForAdmin(
                pageable
            )
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }

    @Transactional(readOnly = true)
    fun getPerformancesByReservationPeriodForAdmin(
        start: LocalDateTime?,
        end: LocalDateTime?, pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        val performances =
            performanceAdminRepository.findUpcomingPerformancesByReservationPeriodForAdmin(
                start, end, pageable
            )
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }

    @Transactional(readOnly = true)
    fun getPerformancesByCategoryForAdmin(
        category: Category,
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        val performances = performanceAdminRepository.findUpcomingPerformancesByCategoryForAdmin(
            category, pageable
        )
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }

    fun getPerformancesSortedByDeletedForAdmin(
        pageable: Pageable
    ): Page<AdminPerformanceResponseDto> {
        val performances =
            performanceAdminRepository.findUpComingPerformancesByDeletedFlagForAdmin(
                pageable
            )
        return toPageAdminPerformanceResponseDto(performances, urlPrefix!!)
    }

    @Transactional(readOnly = true)
    fun getPerformanceStatics(pageable: Pageable): Page<AdminPerformanceStaticsDto> {
        val performanceStatics = performanceAdminRepository.findPerformanceStatics(
            pageable
        )
        if (performanceStatics.isEmpty) {
            throw ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE)
        } else {
            return performanceStatics
        }
    }

    @Transactional(readOnly = true)
    fun getPerformanceBookDetail(performanceId: Long): AdminPerformanceBookDetailDto {
        val performanceBookDetailDtoById =
            performanceRepository.findPerformanceBookDetails(performanceId)
        if (performanceBookDetailDtoById == null) {
            throw ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE)
        } else {
            performanceBookDetailDtoById.urlPrefix = urlPrefix
            return performanceBookDetailDtoById
        }
    }
}
