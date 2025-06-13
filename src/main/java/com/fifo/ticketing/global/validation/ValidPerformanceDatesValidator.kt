package com.fifo.ticketing.global.validation

import com.fifo.ticketing.domain.performance.dto.PerformanceRequestDto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class ValidPerformanceDatesValidator :
    ConstraintValidator<ValidPerformanceDates, PerformanceRequestDto> {
    override fun isValid(
        dto: PerformanceRequestDto?,
        context: ConstraintValidatorContext
    ): Boolean {
        if (dto == null) return true  // null은 다른 @NotNull이 처리

        // startTime < endTime
        if (dto.startTime.isAfter(dto.endTime) || dto.startTime.isEqual(dto.endTime)) {
            return false
        }

        // reservationStartTime <= startTime
        if (dto.reservationStartTime.isAfter(dto.startTime)) {
            return false
        }

        return true
    }
}