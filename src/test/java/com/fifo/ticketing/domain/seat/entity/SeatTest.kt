package com.fifo.ticketing.domain.seat.repository

import com.fifo.ticketing.domain.performance.entity.Category
import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.entity.Place
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.global.entity.File
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("ci")
@DataJpaTest
class SeatVersionJpaTest @Autowired constructor(
    val seatRepository: SeatRepository,
    val entityManager: EntityManager,
) {

    private lateinit var place: Place
    private lateinit var file: File
    private lateinit var performance: Performance
    private lateinit var grade: Grade
    private lateinit var seat: Seat

    @BeforeEach
    fun setUp() {
        place = Place(null, "서울", "테스트장소", 100)
        file = File(null, "poster.jpg", "poster.jpg")
        performance = Performance(
            null, "제목", "설명", place,
            LocalDateTime.of(2025, 6, 1, 19, 0),
            LocalDateTime.of(2025, 6, 1, 21, 0),
            Category.MOVIE, false, false,
            LocalDateTime.of(2025, 5, 1, 19, 0), file
        )
        grade = Grade(null, place, "A", 10000, 10)
        seat = Seat(null, performance, "A1", 10000, grade, SeatStatus.AVAILABLE)
    }

    @Test
    fun `좌석을 저장 후 수정하면 version이 증가한다`() {
        // when
        entityManager.persist(place)
        entityManager.persist(file)
        entityManager.persist(performance)
        entityManager.persist(grade)
        entityManager.persist(seat)
        entityManager.flush()
        entityManager.clear()

        // then
        val savedSeat = seatRepository.findById(seat.id!!).get()
        val versionBefore = savedSeat.version
        savedSeat.book()
        entityManager.flush()
        entityManager.clear()

        val updatedSeat = seatRepository.findById(savedSeat.id!!).get()
        val versionAfter = updatedSeat.version

        updatedSeat.seatStatus shouldBe SeatStatus.BOOKED
        versionAfter shouldBe versionBefore?.plus(1)
    }
}
