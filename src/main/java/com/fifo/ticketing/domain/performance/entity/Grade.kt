package com.fifo.ticketing.domain.performance.entity

import jakarta.persistence.*

@Entity
@Table(name = "grades")
class Grade(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", foreignKey = ForeignKey(name = "fk_grade_to_place"))
    val place: Place? = null,

    @Column(nullable = false)
    val grade: String,

    @Column(nullable = false)
    val defaultPrice: Int,

    @Column(nullable = false)
    val seatCount: Int,
)
