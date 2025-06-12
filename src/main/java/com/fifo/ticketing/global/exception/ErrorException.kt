package com.fifo.ticketing.global.exception

class ErrorException @JvmOverloads constructor(
    val errorCode: ErrorCode,
    val url: String? = null
) : RuntimeException()
