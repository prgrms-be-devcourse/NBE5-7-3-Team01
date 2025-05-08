package com.fifo.ticketing.domain.performance.repository;

import com.fifo.ticketing.domain.performance.entity.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long>{
}
