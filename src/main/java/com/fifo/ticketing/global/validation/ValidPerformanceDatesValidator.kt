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
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("INVALID_DATETIME_PERIOD")
                .addConstraintViolation()
            return false
        }

        // reservationStartTime <= startTime
        if (dto.reservationStartTime.isAfter(dto.startTime)) {
            context.disableDefaultConstraintViolation()
            context.buildConstraintViolationWithTemplate("INVALID_DATETIME_RESERVATION")
                .addConstraintViolation()
            return false
        }

        return true
    }
}