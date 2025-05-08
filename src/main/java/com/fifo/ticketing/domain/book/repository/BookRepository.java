package com.fifo.ticketing.domain.book.repository;

import com.fifo.ticketing.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
