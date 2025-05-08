package com.fifo.ticketing.domain.like.repository;

import com.fifo.ticketing.domain.like.entity.LikeCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCountRepository extends JpaRepository<LikeCount, Long> {
}
