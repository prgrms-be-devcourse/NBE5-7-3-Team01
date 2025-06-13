package com.fifo.ticketing.global.util

import java.time.Duration
import java.time.LocalDateTime

object MillisUtil {

    @JvmStatic
    fun toMillis(runTime: LocalDateTime): Long {
        return Duration.between(LocalDateTime.now(), runTime).toMillis()
    }
}
