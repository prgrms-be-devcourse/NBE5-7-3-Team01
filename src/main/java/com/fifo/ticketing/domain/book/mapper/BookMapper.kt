package com.fifo.ticketing.domain.book.mapper

import com.fifo.ticketing.domain.book.dto.BookCompleteDto
import com.fifo.ticketing.domain.book.dto.BookMailSendDto
import com.fifo.ticketing.domain.book.dto.BookedView
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.entity.BookScheduledTask
import com.fifo.ticketing.domain.book.entity.BookSeat
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.mapper.SeatMapper
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
object BookMapper {
    private const val MAIL_TITLE_PAYED = " 예매가 확정되었습니다"
    private const val MAIL_TITLE_CANCELED = " 예매가 취소되었습니다"
    private const val MAIL_TITLE_DEFAULT = " 예매 상태 안내"

    @JvmStatic
    fun toBookEntity(
        user: User, performance: Performance, totalPrice: Int,
        quantity: Int
    ): Book {
        return Book.create(user, performance, totalPrice, quantity)
    }

    @JvmStatic
    fun toBookSeatEntities(book: Book, seats: List<Seat>): List<BookSeat> {
        return seats.map { BookSeat.of(book, it) }
    }

    @JvmStatic
    fun toBookCompleteDto(book: Book, urlPrefix: String): BookCompleteDto {
        val performance = book.getPerformance()
        return BookCompleteDto(
            performanceId = performance.getId(),
            performanceTitle = performance.getTitle(),
            performanceStartTime = performance.getStartTime(),
            performanceEndTime = performance.getEndTime(),
            placeName = performance.getPlace().getName(),
            encodedFileName = performance.getFile().getEncodedFileName(),
            seats = book.getBookSeats().map { SeatMapper.toBookSeatViewDto(it.getSeat()) },
            totalPrice = book.getTotalPrice(),
            quantity = book.getQuantity(),
            paymentCompleted = false,
            urlPrefix = urlPrefix
        )
    }

    @JvmStatic
    fun toBookedViewDto(book: Book, urlPrefix: String): BookedView {
        val performance = book.getPerformance()

        return BookedView(
            bookId = book.getId()!!,
            performanceId = performance.getId(),
            performanceTitle = performance.getTitle(),
            placeName = performance.getPlace().getName(),
            encodedFileName = performance.getFile().getEncodedFileName(),
            seats = book.getBookSeats().map { SeatMapper.toBookSeatViewDto(it.getSeat()) },
            quantity = book.getQuantity(),
            totalPrice = book.getTotalPrice(),
            bookStatus = book.getBookStatus(),
            urlPrefix = urlPrefix
        )
    }


    @JvmStatic
    fun toBookedViewDtoList(books: Page<Book>, urlPrefix: String): Page<BookedView> {
        return books.map { toBookedViewDto(it, urlPrefix) }
    }


    @JvmStatic
    fun toBookScheduledTaskEntity(bookId: Long?, runtime: LocalDateTime?): BookScheduledTask {
        return BookScheduledTask.create(bookId, runtime)
    }

    @JvmStatic
    fun getBookMailInfo(book: Book): BookMailSendDto {
        val performance = book.getPerformance()
        val user = book.getUser()

        val status = book.getBookStatus()

        val mailTitle = when (status) {
            BookStatus.PAYED -> performance.getTitle() + MAIL_TITLE_PAYED
            BookStatus.CANCELED -> performance.getTitle() + MAIL_TITLE_CANCELED
            else -> MAIL_TITLE_DEFAULT
        }

        return BookMailSendDto(
            emailAddr = user.getEmail(),
            title = mailTitle,
            performanceId = performance.getId(),
            performanceTitle = performance.getTitle(),
            performanceStartTime = performance.getStartTime(),
            performanceEndTime = performance.getEndTime(),
            placeName = performance.getPlace().getName(),
            seats = book.getBookSeats().map { SeatMapper.toBookSeatViewDto(it.getSeat()) },
            totalPrice = book.getTotalPrice(),
            quantity = book.getQuantity(),
            bookStatus = book.getBookStatus(),
        )
    }
}
