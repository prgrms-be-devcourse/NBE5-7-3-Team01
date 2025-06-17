package com.fifo.ticketing.domain.book.entity

import com.fifo.ticketing.global.entity.BaseDateEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "book_scheduled_task")
class BookScheduledTask(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val bookId: Long,

    val scheduledTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var taskStatus: TaskStatus = TaskStatus.PENDING,

    ) : BaseDateEntity() {

    fun complete() {
        this.taskStatus = TaskStatus.COMPLETED
    }

    fun cancel() {
        this.taskStatus = TaskStatus.CANCELED
    }

    companion object {
        fun create(bookId: Long, scheduledTime: LocalDateTime): BookScheduledTask {
            return BookScheduledTask(
                bookId = bookId,
                scheduledTime = scheduledTime,
                taskStatus = TaskStatus.PENDING,
            )
        }
    }
}
