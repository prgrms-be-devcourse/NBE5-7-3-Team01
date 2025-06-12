package com.fifo.ticketing.domain.performance.service

import com.fifo.ticketing.domain.performance.dto.GradeResponseDto
import com.fifo.ticketing.domain.performance.mapper.GradeMapper
import com.fifo.ticketing.domain.performance.repository.GradeRepository
import org.springframework.stereotype.Service

@Service
class GradeService(
    private val gradeRepository: GradeRepository
) {
    fun getGradesByPlaceId(placeId: Long): List<GradeResponseDto> {
        return gradeRepository.findAllByPlaceId(placeId)
            .map { GradeMapper.toDto(it) }
    }
}
