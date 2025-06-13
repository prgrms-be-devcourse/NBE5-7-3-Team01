package com.fifo.ticketing.domain.performance.mapper

import com.fifo.ticketing.domain.performance.dto.GradeResponseDto
import com.fifo.ticketing.domain.performance.entity.Grade

object GradeMapper {
    @JvmStatic
    fun toDto(grade: Grade) = GradeResponseDto(
        grade.grade, grade.seatCount,
        grade.defaultPrice
    )
}
