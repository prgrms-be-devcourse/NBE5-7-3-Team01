package com.fifo.ticketing.global.event;

import com.fifo.ticketing.domain.book.dto.BookMailSendDto;
import com.fifo.ticketing.domain.book.entity.Book;
import com.fifo.ticketing.domain.book.service.BookService;
import com.fifo.ticketing.global.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PerformanceCancelMailListener {

    private final MailService mailService;
    private final BookService bookService;

    @Async("cancelPerformanceMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePerformanceCancelMailEvent(PerformanceCanceledEvent event) {
        for (Book book : event.getCanceledBooks()) {
            BookMailSendDto bookMailSendDto = bookService.getBookMailInfo(book.getId());
            mailService.sendPerformanceCancelNoticeMail(bookMailSendDto);
        }
    }
}
