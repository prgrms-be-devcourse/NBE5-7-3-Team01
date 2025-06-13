package com.fifo.ticketing.domain.performance.entity

import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto
import com.fifo.ticketing.global.entity.BaseDateEntity
import com.fifo.ticketing.global.entity.File
import jakarta.persistence.*
import lombok.Setter
import java.time.LocalDateTime

@Entity
@Table(name = "performances")
class Performance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", foreignKey = ForeignKey(name = "fk_performance_to_place"))
    var place: Place,

    @Column(nullable = false)
    var startTime: LocalDateTime,

    @Column(nullable = false)
    var endTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var category: Category,

    @Column(nullable = false)
    var performanceStatus: Boolean,

    @Column(nullable = false)
    var deletedFlag: Boolean,

    @Column(nullable = false)
    var reservationStartTime: LocalDateTime,

    @Setter
    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "file_id", foreignKey = ForeignKey(name = "fk_performance_to_file"))
    var file: File? = null,

    ) : BaseDateEntity() {
    fun update(dto: PerformanceRequestDto, place: Place) {
        this.title = dto.title
        this.description = dto.description
        this.place = place
        this.startTime = dto.startTime
        this.endTime = dto.endTime
        this.category = dto.category
        this.performanceStatus = dto.performanceStatus
        this.reservationStartTime = dto.reservationStartTime
    }

    fun delete() {
        this.performanceStatus = false
        this.deletedFlag = true
    }
}
