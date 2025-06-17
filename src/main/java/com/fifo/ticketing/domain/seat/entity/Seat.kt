package com.fifo.ticketing.domain.seat.entity

import com.fifo.ticketing.domain.performance.entity.Grade
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.global.entity.BaseDateEntity
import com.fifo.ticketing.global.exception.AlertDetailException
import com.fifo.ticketing.global.exception.ErrorCode
import jakarta.persistence.*
import lombok.Getter
import lombok.NoArgsConstructor
import org.hibernate.annotations.BatchSize

@Entity
@Getter
@Table(name = "seats")
@NoArgsConstructor
@BatchSize(size = 100)
class Seat // Version의 경우는 JPA가 Persist 시에 자동으로 생성하기 때문에 생성자에 추가하지 않아도 됩니다!
    (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,

    @JoinColumn(name = "performance_id", foreignKey = ForeignKey(name = "fk_seat_to_performance"))
    @ManyToOne(fetch = FetchType.LAZY)
    val performance: Performance,

    @Column(
        name = "seat_number",
        nullable = false
    )
    var seatNumber: String,

    @Column(nullable = false)
    var price: Int,

    @JoinColumn(
        name = "grade_id",
        foreignKey = ForeignKey(name = "fk_seat_to_grade")
    )
    @ManyToOne(fetch = FetchType.LAZY)
    val grade: Grade,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var seatStatus: SeatStatus

) : BaseDateEntity() {
    @Version
    val version: Long? = null

    fun book() {
        this.seatStatus = SeatStatus.BOOKED
    }

    fun available() {
        this.seatStatus = SeatStatus.AVAILABLE
    }

    fun occupy() {
        this.seatStatus = SeatStatus.OCCUPIED
    }

    fun validateAvailable() {
        if (seatStatus != SeatStatus.AVAILABLE) {
            throw AlertDetailException(
                message = "${id}번 좌석은 예약할 수 없는 상태입니다. (현재 상태: $seatStatus)",
                errorCode = ErrorCode.SEAT_ALREADY_BOOKED
            )
        }
    }

    companion object {
        @JvmStatic
        fun of(performance: Performance, grade: Grade, number: Int): Seat {
            return Seat(
                null, performance, grade.grade + number, grade.defaultPrice,
                grade, SeatStatus.AVAILABLE
            )
        }
    }
}
