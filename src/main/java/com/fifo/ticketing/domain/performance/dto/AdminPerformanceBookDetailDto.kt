package com.fifo.ticketing.domain.performance.dto

class AdminPerformanceBookDetailDto(
    val id: Long?,
    val title: String?,
    val encodedFileName: String?,
    val totalPrice: Long?,
    val totalQuantity: Long?,
) {
    var urlPrefix: String? = null

    val url: String
        get() = urlPrefix + encodedFileName
}