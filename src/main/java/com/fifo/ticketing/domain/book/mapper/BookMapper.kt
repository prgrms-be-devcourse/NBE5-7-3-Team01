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
        val performance = book.performance
        return BookCompleteDto(
            // 해당 부분은 performance의 id가 존재하지 않는 경우가 없기 때문에 null Assert 처리
            performanceId = performance.id!!,
            performanceTitle = performance.title,
            performanceStartTime = performance.startTime,
            performanceEndTime = performance.endTime,
            placeName = performance.place.name,
            encodedFileName = performance.file!!.encodedFileName,
            seats = book.bookSeats.map { SeatMapper.toBookSeatViewDto(it.seat) },
            totalPrice = book.totalPrice,
            quantity = book.quantity,
            paymentCompleted = false,
            urlPrefix = urlPrefix
        )
    }

    @JvmStatic
    fun toBookedViewDto(book: Book, urlPrefix: String): BookedView {
        val performance = book.performance

        return BookedView(
            bookId = book.id!!,
            performanceId = performance.id!!,
            performanceTitle = performance.title,
            placeName = performance.place.name,
            encodedFileName = performance.file!!.encodedFileName,
            seats = book.bookSeats.map { SeatMapper.toBookSeatViewDto(it.seat) },
            quantity = book.quantity,
            totalPrice = book.totalPrice,
            bookStatus = book.bookStatus,
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
        val performance = book.performance
        val user = book.user

        val status = book.getBookStatus()

        val mailTitle = when (status) {
            BookStatus.PAYED -> performance.title + MAIL_TITLE_PAYED
            BookStatus.CANCELED -> performance.title + MAIL_TITLE_CANCELED
            else -> MAIL_TITLE_DEFAULT
        }

        return BookMailSendDto(
            emailAddr = user.email,
            title = mailTitle,
            performanceId = performance.id!!,
            performanceTitle = performance.title,
            performanceStartTime = performance.startTime,
            performanceEndTime = performance.endTime,
            placeName = performance.place.name,
            seats = book.bookSeats.map { SeatMapper.toBookSeatViewDto(it.seat) },
            totalPrice = book.totalPrice,
            quantity = book.quantity,
            bookStatus = book.bookStatus,
        )
    }
}
