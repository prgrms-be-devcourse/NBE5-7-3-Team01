package com.fifo.ticketing.domain.performance.entity

import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.NoArgsConstructor

@Entity
@Table(name = "places")
class Place
    (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val address: String,

    @Column(nullable = false)
    val name: String,

    @Column(name = "total_seats", nullable = false)
    val totalSeats: Int,
)

