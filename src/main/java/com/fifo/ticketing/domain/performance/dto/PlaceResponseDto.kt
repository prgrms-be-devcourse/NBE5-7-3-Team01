package com.fifo.ticketing.domain.performance.dto

data class PlaceResponseDto(
    val id: Long? = null,
    val name: String? = null,
    val address: String? = null,
    val totalSeats: Int? = null,
) {
    constructor(id: Long, name: String) : this(id, name, null, null)
}
