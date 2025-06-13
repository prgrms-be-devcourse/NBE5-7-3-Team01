package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.dto.BookCreateRequest
import com.fifo.ticketing.domain.book.dto.BookedView
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.mapper.BookMapper
import com.fifo.ticketing.domain.book.mapper.BookMapper.toBookEntity
import com.fifo.ticketing.domain.book.mapper.BookMapper.toBookSeatEntities
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.repository.BookSeatRepository
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.performance.repository.PerformanceRepository
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.entity.SeatStatus
import com.fifo.ticketing.domain.seat.service.SeatService
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.domain.user.repository.UserRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BookKtService(

    @Value("\${file.url-prefix}")
    private var urlPrefix: String,

    private val bookRepository: BookRepository,
    private val bookSeatRepository: BookSeatRepository,
    private val bookScheduleManager: BookScheduleManager,
    private val userRepository: UserRepository,
    private val performanceRepository: PerformanceRepository,
    private val seatService: SeatService,

) {
    @Transactional
    suspend fun createBook(performanceId: Long, userId: Long, request: BookCreateRequest): Long {
        val user: User = userRepository.findById(userId)
            .orElseThrow{ ErrorException(ErrorCode.NOT_FOUND_MEMBER) }


        val performance = performanceRepository.findByIdOrNull(performanceId)
            ?: throw ErrorException(ErrorCode.NOT_FOUND_PERFORMANCE)


        val selectedSeats: List<Seat> = seatService.validateBookSeats(request.seatIds)

        val totalPrice = selectedSeats.stream().mapToInt { obj: Seat -> obj.price }.sum()
        val quantity = selectedSeats.size

        val book: Book =
            saveBookAndBookSeats(user, performance, totalPrice, quantity, selectedSeats)

        scheduleBookCancel(book.id)

        return book.id
    }


    suspend fun scheduleBookCancel(bookId: Long) {
        val runTime = LocalDateTime.now().plusMinutes(10)
        bookScheduleManager.scheduleCancelTask(bookId, runTime)
    }

    fun saveBookAndBookSeats(
        user: User, performance: Performance, totalPrice: Int,
        quantity: Int,
        selectedSeats: List<Seat>
    ): Book {
        val book = toBookEntity(user, performance, totalPrice, quantity)
        bookRepository.save(book)
        bookRepository.flush()

        val bookSeatList = toBookSeatEntities(book, selectedSeats)

        bookSeatRepository.saveAll(bookSeatList)
        return book
    }

    @Transactional
    fun cancelBook(bookId: Long, userId: Long): Long {
        val book: Book = bookRepository.findByUserIdAndId(userId, bookId) ?: throw ErrorException(
            ErrorCode.NOT_FOUND_BOOK,
            "예매 정보가 존재하지 않습니다.",
        )

        val bookSeats: List<BookSeat> = bookSeatRepository.findAllByBookId(book.id)

        book.canceled()

        SeatService.changeSeatStatus(bookSeats, SeatStatus.AVAILABLE)

        return bookId
    }

    @Transactional
    fun getBookDetail(userId: Long, bookId: Long): BookedView {
        val book: Book = bookRepository.findByUserIdAndId(userId, bookId) ?: throw ErrorException(
            ErrorCode.NOT_FOUND_BOOK,
            "예매 정보가 존재하지 않습니다."
        )

        return BookMapper.toBookedViewDto(book, urlPrefix)
    }
}