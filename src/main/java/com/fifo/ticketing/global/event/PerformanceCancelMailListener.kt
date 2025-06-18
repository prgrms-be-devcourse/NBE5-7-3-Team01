package com.fifo.ticketing.global.event

import com.fifo.ticketing.domain.book.dto.BookMailSendDto
import com.fifo.ticketing.domain.book.entity.Book
import com.fifo.ticketing.domain.book.service.BookService
import com.fifo.ticketing.global.service.MailService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PerformanceCancelMailListener(
    private val mailService: MailService,
    private val bookService: BookService
) {

    @Async("cancelPerformanceMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handlePerformanceCancelMailEvent(event: PerformanceCanceledEvent) {
        event.canceledBooks.forEach { book: Book ->
            val bookMailSendDto: BookMailSendDto = bookService.getBookMailInfo(book.id!!)
            mailService.sendPerformanceCancelNoticeMail(bookMailSendDto)
        }
    }
}
