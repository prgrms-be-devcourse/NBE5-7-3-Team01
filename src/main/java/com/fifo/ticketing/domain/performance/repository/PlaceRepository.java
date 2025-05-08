package com.fifo.ticketing.domain.performance.repository;

import com.fifo.ticketing.domain.performance.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
}
