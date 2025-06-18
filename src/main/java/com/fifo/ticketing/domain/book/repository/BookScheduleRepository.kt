package com.fifo.ticketing.domain.book.repository

import com.fifo.ticketing.domain.book.entity.BookScheduledTask
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface BookScheduleRepository : JpaRepository<BookScheduledTask, Long> {

    @Query("SELECT s FROM BookScheduledTask s WHERE s.taskStatus = 'PENDING'")
    fun findAllPendingTasks(): List<BookScheduledTask>
}
