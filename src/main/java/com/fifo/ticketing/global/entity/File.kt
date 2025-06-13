package com.fifo.ticketing.global.entity

import jakarta.persistence.*

@Entity
@Table(name = "files")
class File @JvmOverloads constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var encodedFileName: String,

    var originalFileName: String
) : BaseDateEntity() {

    fun update(encodedFileName: String, originalFileName: String) {
        this.encodedFileName = encodedFileName
        this.originalFileName = originalFileName
    }
}
