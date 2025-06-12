package com.fifo.ticketing.domain.performance.repository

import com.fifo.ticketing.domain.performance.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, Long>
