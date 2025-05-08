package com.fifo.ticketing.domain.user.repository;

import com.fifo.ticketing.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
