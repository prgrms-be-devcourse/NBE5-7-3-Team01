package com.fifo.ticketing.domain.performance.mapper;

import com.fifo.ticketing.domain.performance.dto.GradeResponseDto;
import com.fifo.ticketing.domain.performance.entity.Grade;

public class GradeMapper {

    public static GradeResponseDto toDto(Grade grade) {
        return new GradeResponseDto(grade.getGrade(), grade.getSeatCount(),
                grade.getDefaultPrice());
    }
}
