package com.fifo.ticketing.global.exception

class ErrorException @JvmOverloads constructor(
    val url: String? = null,
    val errorCode: ErrorCode
) : RuntimeException()
