package com.fifo.ticketing.domain.book.repository

import com.fifo.ticketing.domain.book.dto.BookAdminDetailDto
import com.fifo.ticketing.domain.book.dto.BookUserDetailDto
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface BookRepository : JpaRepository<Book, Long> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Book>

    fun findByUserIdAndId(userId: Long, bookId: Long): Book?

    @Query("""
        SELECT b FROM Book b
        JOIN FETCH b.user
        JOIN FETCH b.performance
        WHERE b.performance = :performance AND b.bookStatus = :bookStatus
    """)
    fun findAllWithUserAndPerformanceByPerformanceAndBookStatus(
        @Param("performance") performance: Performance,
        @Param("bookStatus") bookStatus: BookStatus
    ): List<Book>

    @Query("""
        SELECT b FROM Book b 
        WHERE b.user.id = :userId 
        AND b.bookStatus = :bookStatus 
        ORDER BY b.createdAt DESC
    """)
    fun findAllByUserIdAndBookStatus(
        @Param("userId") userId: Long,
        @Param("bookStatus") bookStatus: BookStatus,
        pageable: Pageable
    ): Page<Book>

    @Query("""
        SELECT b FROM Book b 
        WHERE b.user.id = :userId
        AND b.performance.title LIKE %:performanceTitle% 
        AND b.bookStatus = :bookStatus
        ORDER BY b.createdAt DESC
    """)
    fun findAllByUserIdAndTitleAndBookStatus(
        @Param("userId") userId: Long,
        @Param("performanceTitle") performanceTitle: String,
        @Param("bookStatus") bookStatus: BookStatus,
        pageable: Pageable
    ): Page<Book>

    @Query("""
        SELECT b FROM Book b
        WHERE b.user.id = :userId
        AND b.performance.title LIKE %:performanceTitle%
        ORDER BY b.createdAt DESC
    """)
    fun findAllByUserIdAndTitle(
        @Param("userId") userId: Long,
        @Param("performanceTitle") performanceTitle: String,
        pageable: Pageable
    ): Page<Book>

    @Modifying
    @Query("""
        UPDATE Book b 
        SET b.bookStatus = :cancelStatus 
        WHERE b.performance = :performance AND b.bookStatus = :currentStatus
    """)
    fun cancelAllByPerformance(
        @Param("performance") performance: Performance,
        @Param("cancelStatus") cancelStatus: BookStatus,
        @Param("currentStatus") currentStatus: BookStatus
    )

    @Query("""
        SELECT b.id, u.username, b.totalPrice, b.quantity
        FROM Book b 
        JOIN User u on b.user.id = u.id
        WHERE b.performance.id = :performanceId
    """)
    fun findAllBookDetailsAdmin(
        @Param("performanceId") id: Long,
        pageable: Pageable
    ): Page<BookAdminDetailDto>

    @Query("""
        SELECT new com.fifo.ticketing.domain.book.dto.BookUserDetailDto(
            b.id, p.id, p.title, b.totalPrice, b.quantity, u.username, f.encodedFileName, b.bookStatus)
        FROM Book b 
        JOIN Performance p ON b.performance.id = p.id 
        JOIN User u ON b.user.id = u.id 
        LEFT JOIN File f ON p.file.id = f.id 
        WHERE b.id = :bookId AND p.id = :performanceId
    """)
    fun findBookDetailByBookId(
        @Param("bookId") bookId: Long,
        @Param("performanceId") performanceId: Long
    ): BookUserDetailDto?

    fun existsByUserAndPerformanceAndBookStatus(
        user: User, performance: Performance,
        bookStatus: BookStatus
    ): Boolean
}

