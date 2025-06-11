package com.fifo.ticketing.domain.book.entity

import com.fifo.ticketing.domain.seat.entity.Seat
import com.fifo.ticketing.global.entity.BaseDateEntity
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Table(name = "book_seats")
class BookSeat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", foreignKey = ForeignKey(name = "fk_book_seat_to_books"))
    val book: Book,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", foreignKey = ForeignKey(name = "fk_book_seat_to_seat"))
    val seat: Seat,

    ) :BaseDateEntity() {

    companion object {
        fun of(book: Book, seat: Seat): BookSeat {
            return BookSeat(
                book = (book),
                seat = (seat)
            );
        }
    }
}

