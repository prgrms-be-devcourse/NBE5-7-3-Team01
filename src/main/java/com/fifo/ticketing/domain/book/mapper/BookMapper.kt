package com.fifo.ticketing.domain.book.mapper

import com.fifo.ticketing.domain.book.dto.BookCompleteDto
import com.fifo.ticketing.domain.book.dto.BookMailSendDto
import com.fifo.ticketing.domain.book.dto.BookedView
import com.fifo.ticketing.domain.book.entity.*
import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.domain.seat.mapper.toBookSeatViewDto
import com.fifo.ticketing.domain.user.entity.User
import org.springframework.data.domain.Page
import java.time.LocalDateTime

fun User.toBook(
    performance: Performance,
    totalPrice: Int,
    quantity: Int
): Book = Book.create(this, performance, totalPrice, quantity)

fun Book.toBookSeatList(seats: List<Seat>): List<BookSeat> =
    seats.map { BookSeat.of(this, it) }

fun Book.toBookCompleteDto(urlPrefix: String): BookCompleteDto {
    val performance = this.performance
    return BookCompleteDto(
        performanceId = performance.id!!,
        performanceTitle = performance.title,
        performanceStartTime = performance.startTime,
        performanceEndTime = performance.endTime,
        placeName = performance.place.name,
        encodedFileName = performance.file!!.encodedFileName,
        seats = this.bookSeats.map { it.seat.toBookSeatViewDto() },
        totalPrice = this.totalPrice,
        quantity = this.quantity,
        paymentCompleted = false,
        urlPrefix = urlPrefix
    )
}

fun Book.toBookedView(urlPrefix: String): BookedView {
    val performance = this.performance
    return BookedView(
        bookId = this.id!!,
        performanceId = performance.id!!,
        performanceTitle = performance.title,
        placeName = performance.place.name,
        encodedFileName = performance.file!!.encodedFileName,
        seats = this.bookSeats.map { it.seat.toBookSeatViewDto() },
        quantity = this.quantity,
        totalPrice = this.totalPrice,
        bookStatus = this.bookStatus,
        urlPrefix = urlPrefix
    )
}

fun Page<Book>.toBookedViewDtoList(urlPrefix: String): Page<BookedView> =
    this.map { it.toBookedView(urlPrefix) }

fun Long.toBookScheduledTask(runtime: LocalDateTime): BookScheduledTask =
    BookScheduledTask.create(this, runtime)

fun Book.toBookMailSendDto(): BookMailSendDto {
    val performance = this.performance
    val user = this.user

    val titleSuffix = when (this.bookStatus) {
        BookStatus.PAYED -> " 예매가 확정되었습니다"
        BookStatus.CANCELED -> " 예매가 취소되었습니다"
        else -> " 예매 상태 안내"
    }

    return BookMailSendDto(
        emailAddr = user.email,
        title = performance.title + titleSuffix,
        performanceId = performance.id!!,
        performanceTitle = performance.title,
        performanceStartTime = performance.startTime,
        performanceEndTime = performance.endTime,
        placeName = performance.place.name,
        seats = this.bookSeats.map { it.seat.toBookSeatViewDto() },
        totalPrice = this.totalPrice,
        quantity = this.quantity,
        bookStatus = this.bookStatus,
    )
}
