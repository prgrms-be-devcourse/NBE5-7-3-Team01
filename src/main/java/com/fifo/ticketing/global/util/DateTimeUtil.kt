package com.fifo.ticketing.global.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object DateTimeUtil {

    @JvmStatic
    fun toDate(localDateTime: LocalDateTime): Date {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
}
