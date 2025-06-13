package com.fifo.ticketing.domain.book.service

import com.fifo.ticketing.domain.book.repository.BookScheduleRepository
import com.fifo.ticketing.global.util.MillisUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class BookScheduleInitializer(
    private val bookScheduleManager: BookScheduleManager,
    private val bookScheduleRepository: BookScheduleRepository,

    private val coroutineScope: CoroutineScope
) {

    // EventListener에 suspend를 붙이면 코루틴이 인식되지 않음
    @EventListener(ApplicationReadyEvent::class)
    fun reScheduleUnpaidBooks() {
        val pendingTasks = bookScheduleRepository.findAllPendingTasks()

        for (pendingTask in pendingTasks) {
            val triggerTime = MillisUtil.toMillis(pendingTask.scheduledTime)

            coroutineScope.launch {
                delay(triggerTime)
                bookScheduleManager.cancelIfUnpaid(pendingTask.bookId)
            }

        }
    }
}
