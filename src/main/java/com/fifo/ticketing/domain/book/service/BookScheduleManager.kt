package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.entity.BookScheduledTask
import com.fifo.ticketing.domain.book.entity.BookStatus
import com.fifo.ticketing.domain.book.mapper.BookMapper.toBookScheduledTaskEntity
import com.fifo.ticketing.domain.book.repository.BookRepository
import com.fifo.ticketing.domain.book.repository.BookScheduleRepository
import com.fifo.ticketing.domain.book.repository.BookSeatRepository
import com.fifo.ticketing.domain.seat.repository.SeatRepository
import com.fifo.ticketing.global.exception.ErrorCode
import com.fifo.ticketing.global.exception.ErrorException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}
@Service
class BookScheduleManager(
    private val bookScheduleRepository: BookScheduleRepository,
    private val bookRepository: BookRepository,
    private val bookSeatRepository: BookSeatRepository,
    private val seatRepository: SeatRepository,


    private val coroutineScope: CoroutineScope,

    ) {


    @Transactional
    suspend fun scheduleCancelTask(bookId: Long, runTime: LocalDateTime) {
        bookScheduleRepository.save(toBookScheduledTaskEntity(bookId, runTime))

        log.info{"${bookId}번 예매 생성됨 | ${LocalDateTime.now()}"}


        // 레포지토리에 작업이 저장되면 실행됨
        coroutineScope.launch {
            // 10분동안 대기했다가
            delay(600000)
            // 취소 로직 실행
            cancelIfUnpaid(bookId)
        }

    }

    @Transactional
    suspend fun cancelIfUnpaid(bookId: Long) {
        val book = bookRepository.findById(bookId)
            .orElseThrow { ErrorException(ErrorCode.NOT_FOUND_BOOK) }

        if (book.bookStatus == BookStatus.CONFIRMED) {
            book.canceled()

            bookRepository.save(book)

            log.info{"${bookId}번 예매 취소됨 | ${LocalDateTime.now()}"}

            val bookSeats = bookSeatRepository.findAllByBookIdWithSeat(book.id)
            bookSeats.map {
                it.seat.available()
                seatRepository.save(it.seat)
            }
        }

        Optional.ofNullable(book.scheduledTask)
            .ifPresent { obj: BookScheduledTask -> obj.complete() }
    }
}
