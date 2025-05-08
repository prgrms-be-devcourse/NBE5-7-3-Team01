package com.fifo.ticketing.domain.book.repository;

import com.fifo.ticketing.domain.book.entity.BookSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookSeatRepository extends JpaRepository<BookSeat, Long> {
}
