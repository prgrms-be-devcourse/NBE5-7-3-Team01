package com.fifo.ticketing.domain.like.repository

import com.fifo.ticketing.domain.like.entity.Like
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface LikeRepository : JpaRepository<Like, Long> {

    fun findByUserAndPerformance(user: User, performance: Performance): Like?

    @Query("SELECT l.user FROM Like l WHERE l.performance.id = :performanceId AND l.isLiked = true")
    fun findUsersByPerformanceId(@Param("performanceId") performanceId: Long): List<User>

    @Query("SELECT l FROM Like l WHERE l.isLiked =true AND l.performance.reservationStartTime BETWEEN :start AND :end")
    fun findLikesByTargetTime(@Param("start") start: LocalDateTime, @Param("end") end: LocalDateTime): List<Like>

    @Query("SELECT l.performance.id FROM Like l WHERE l.user.id = :userId AND l.isLiked = true")
    fun findLikedPerformanceIdsByUserId(@Param("userId") userId: Long): List<Long>

    @Query("SELECT l.performance FROM Like l WHERE l.performance.deletedFlag != true AND l.user.id = :userId AND l.isLiked = true ORDER BY l.performance.startTime ASC")
    fun findLikedPerformancesByUserId(@Param("userId") userId: Long, pageable: Pageable): Page<Performance>
}
