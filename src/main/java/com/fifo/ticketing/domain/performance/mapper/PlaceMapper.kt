package com.fifo.ticketing.domain.performance.mapper

import com.fifo.ticketing.domain.performance.dto.PlaceResponseDto
import com.fifo.ticketing.domain.performance.entity.Place

object PlaceMapper {
    @JvmStatic
    fun toDtoForPerformanceCreate(place: Place) = PlaceResponseDto(place.id, place.name)
}
