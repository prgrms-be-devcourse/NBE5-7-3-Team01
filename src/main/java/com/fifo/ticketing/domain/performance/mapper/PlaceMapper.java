package com.fifo.ticketing.domain.performance.mapper;

import com.fifo.ticketing.domain.performance.dto.PlaceResponseDto;
import com.fifo.ticketing.domain.performance.entity.Place;

public class PlaceMapper {

    public static PlaceResponseDto toDtoForPerformanceCreate(Place place) {
        return new PlaceResponseDto(place.getId(), place.getName());
    }
}
