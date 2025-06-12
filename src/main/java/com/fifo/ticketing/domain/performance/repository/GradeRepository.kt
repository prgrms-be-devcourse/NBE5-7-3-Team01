package com.fifo.ticketing.domain.performance.repository

import com.fifo.ticketing.domain.performance.entity.Grade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GradeRepository : JpaRepository<Grade, Long> {
    fun findAllByPlaceId(id: Long): List<Grade>
}
