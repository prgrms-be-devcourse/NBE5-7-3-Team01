package com.fifo.ticketing.domain.book.entity

import com.fifo.ticketing.domain.performance.entity.Performance
import com.fifo.ticketing.domain.user.entity.User
import com.fifo.ticketing.global.entity.BaseDateEntity
import com.fifo.ticketing.global.entity.File
import jakarta.persistence.*

@Entity
@Table(name = "books")
class Book(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    val performance: Performance,

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookSeats: List<BookSeat> = emptyList(),

    @OneToOne
    @JoinColumn(name = "file_id")
    val file: File? = null,

    @Enumerated(EnumType.STRING)
    var bookStatus: BookStatus = BookStatus.CONFIRMED,

    @Column(nullable = false)
    var totalPrice: Int,

    @Column(nullable = false)
    var quantity: Int,

    @OneToOne
    @JoinColumn(name = "task_id")
    val scheduledTask: BookScheduledTask? = null

) : BaseDateEntity() {

    fun canceled() {
        this.bookStatus = BookStatus.CANCELED
    }

    fun payed() {
        this.bookStatus = BookStatus.PAYED
    }

    companion object {
        fun create(user: User, performance: Performance, totalPrice: Int, quantity: Int): Book {
            return Book(
                user = user,
                performance = performance,
                totalPrice = totalPrice,
                quantity = quantity
            )
        }
    }
}
