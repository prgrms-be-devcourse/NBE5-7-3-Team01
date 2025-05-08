package com.fifo.ticketing.domain.like.repository;

import com.fifo.ticketing.domain.like.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
}
