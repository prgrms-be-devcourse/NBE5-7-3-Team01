package com.fifo.ticketing.global.repository

import com.fifo.ticketing.global.entity.File
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FileRepository : JpaRepository<File, Long>
