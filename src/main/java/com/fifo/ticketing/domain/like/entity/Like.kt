package com.fifo.ticketing.domain.like.entity

import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.global.entity.BaseDateEntity
import jakarta.persistence.*


@Entity
@Table(name = "likes")
class Like(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_like_to_user")
    )
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "performance_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_like_to_performance")
    )
    val performance: Performance,

    @Column(name = "is_liked")
    var isLiked: Boolean
) : BaseDateEntity() 