package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.dto.BookedView
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.mapper.BookMapper
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.repository.BookSeatRepository
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BookKtService(

    @Value("\${file.url-prefix}")
    private var urlPrefix: String,

    private val bookRepository: BookRepository,
    private val bookSeatRepository: BookSeatRepository
) {

    @Transactional
    fun cancelBook(bookId: Long, userId: Long): Long {
        val book: Book = bookRepository.findByUserIdAndId(userId, bookId) ?: throw ErrorException(ErrorCode.NOT_FOUND_BOOK)

        val bookSeats: List<BookSeat> = bookSeatRepository.findAllByBookId(book.id)

        book.canceled()

        SeatService.changeSeatStatus(bookSeats, SeatStatus.AVAILABLE)

        return bookId
    }

    @Transactional
    fun getBookDetail(userId: Long, bookId: Long): BookedView {
        val book: Book = bookRepository.findByUserIdAndId(userId, bookId) ?: throw ErrorException(ErrorCode.NOT_FOUND_BOOK);

        return BookMapper.toBookedViewDto(book, urlPrefix)
    }
}