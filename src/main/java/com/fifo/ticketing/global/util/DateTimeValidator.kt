package com.fifo.ticketing.global.util

import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import java.time.LocalDateTime

object DateTimeValidator {

    @JvmStatic
    fun periodValidator(startDate: LocalDateTime?, endDate: LocalDateTime?) {
        if (startDate == null || endDate == null) {
            throw ErrorException(ErrorCode.INVALID_DATETIME_TYPE)
        }
        if (startDate.isAfter(endDate)) {
            throw ErrorException(ErrorCode.INVALID_DATETIME_PERIOD)
        }
    }
}
