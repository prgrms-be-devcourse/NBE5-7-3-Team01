package com.fifo.ticketing.global.exception

class AlertDetailException @JvmOverloads constructor(
    val url: String? = null,
    override val message: String,
    val errorCode: ErrorCode
) : RuntimeException(message)

