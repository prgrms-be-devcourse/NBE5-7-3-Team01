package com.fifo.ticketing.domain.like.entity

import com.fifo.ticketing.domain.performance.entity.Performance
import jakarta.persistence.*


@Entity
@Table(name = "like_count")
class LikeCount(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    @JoinColumn(name = "performance_id", nullable = false, foreignKey = ForeignKey(name = "fk_like_count_performance"))
    val performance: Performance,

    @Column(nullable = false)
    var likeCount: Long,
)