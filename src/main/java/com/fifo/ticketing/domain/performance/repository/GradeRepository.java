package com.fifo.ticketing.domain.performance.repository;

import com.fifo.ticketing.domain.performance.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}
