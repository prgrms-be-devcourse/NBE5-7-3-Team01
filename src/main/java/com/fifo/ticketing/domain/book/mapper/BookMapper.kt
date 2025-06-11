package com.fifo.ticketing.domain.book.mapper

import com.fifo.ticketing.domain.book.dto.BookCompleteDto
import com.fifo.ticketing.domain.book.dto.BookMailSendDto
import com.fifo.ticketing.domain.book.dto.BookSeatViewDto
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
import java.util.stream.Collectors

@Component
object BookMapper {
    private const val MAIL_TITLE_PAYED = " 예매가 확정되었습니다"
    private const val MAIL_TITLE_CANCELED = " 예매가 취소되었습니다"
    private const val MAIL_TITLE_DEFAULT = " 예매 상태 안내"

    fun toBookEntity(
        user: User?, performance: Performance?, totalPrice: Int,
        quantity: Int
    ): Book {
        return Book.create(user, performance, totalPrice, quantity)
    }

    fun toBookSeatEntities(book: Book?, seats: List<Seat?>): List<BookSeat> {
        return seats.stream()
            .map { seat: Seat? -> BookSeat.of(book, seat) }
            .collect(Collectors.toList())
    }

    fun toBookCompleteDto(book: Book, urlPrefix: String?): BookCompleteDto {
        return BookCompleteDto.builder()
            .performanceId(book.performance.id)
            .performanceTitle(book.performance.title)
            .performanceStartTime(book.performance.startTime)
            .performanceEndTime(book.performance.endTime)
            .placeName(book.performance.place.name)
            .encodedFileName(book.performance.file.encodedFileName)
            .seats(book.bookSeats.stream()
                .map<BookSeatViewDto> { bs: BookSeat -> SeatMapper.toBookSeatViewDto(bs.seat) }
                .collect(Collectors.toList()))
            .totalPrice(book.totalPrice)
            .quantity(book.quantity)
            .paymentCompleted(false)
            .urlPrefix(urlPrefix)
            .build()
    }

    fun toBookedViewDto(book: Book, urlPrefix: String?): BookedView {
        val performance = book.performance

        return BookedView.builder()
            .bookId(book.id)
            .performanceId(performance.id)
            .performanceTitle(performance.title)
            .placeName(performance.place.name)
            .encodedFileName(performance.file.encodedFileName)
            .seats(book.bookSeats.stream()
                .map<BookSeatViewDto> { bs: BookSeat -> SeatMapper.toBookSeatViewDto(bs.seat) }
                .collect(Collectors.toList()))
            .quantity(book.quantity)
            .totalPrice(book.totalPrice)
            .bookStatus(book.bookStatus)
            .urlPrefix(urlPrefix)
            .build()
    }

    fun toBookedViewDtoList(books: Page<Book>, urlPrefix: String?): Page<BookedView> {
        return books.map { book: Book -> toBookedViewDto(book, urlPrefix) }
    }

    fun toBookScheduledTaskEntity(bookId: Long?, runtime: LocalDateTime?): BookScheduledTask {
        return BookScheduledTask.create(bookId, runtime)
    }

    fun getBookMailInfo(book: Book): BookMailSendDto {
        val performance = book.performance
        val user = book.user

        val status = book.bookStatus

        val mailTitle = when (status) {
            BookStatus.PAYED -> performance.title + MAIL_TITLE_PAYED
            BookStatus.CANCELED -> performance.title + MAIL_TITLE_CANCELED
            else -> MAIL_TITLE_DEFAULT
        }

        return BookMailSendDto.builder()
            .emailAddr(user.email)
            .title(mailTitle)
            .performanceId(performance.id)
            .performanceTitle(performance.title)
            .performanceStartTime(performance.startTime)
            .performanceEndTime(performance.endTime)
            .placeName(performance.place.name)
            .seats(book.bookSeats.stream()
                .map<BookSeatViewDto> { bs: BookSeat -> SeatMapper.toBookSeatViewDto(bs.seat) }
                .collect(Collectors.toList()))
            .totalPrice(book.totalPrice)
            .quantity(book.quantity)
            .bookStatus(book.bookStatus)
            .build()
    }
}
