package com.fifo.ticketing.domain.book.entity;

import com.fifo.ticketing.domain.performance.entity.Performance;
import com.fifo.ticketing.domain.seat.entity.SeatStatus;
import com.fifo.ticketing.domain.user.entity.User;
import com.fifo.ticketing.global.entity.BaseDateEntity;
import com.fifo.ticketing.global.entity.File;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
public class Book extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id", nullable = false)
    private Performance performance;

    @Builder.Default
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookSeat> bookSeats = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "file_id")
    private File file;

    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

    @Column(nullable = false)
    private Integer totalPrice;

    @Column(nullable = false)
    private Integer quantity;

    @OneToOne
    @JoinColumn(name = "task_id")
    private BookScheduledTask scheduledTask;

    public void canceled() {
        this.bookStatus = bookStatus.CANCELED;
    }
    public void payed() {this.bookStatus = bookStatus.PAYED;}

    public static Book create(User user, Performance performance, int totalPrice, int quantity) {
        return Book.builder()
            .user(user)
            .performance(performance)
            .totalPrice(totalPrice)
            .quantity(quantity)
            .bookStatus(BookStatus.CONFIRMED)
            .build();

    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Performance getPerformance() {
        return performance;
    }

    public List<BookSeat> getBookSeats() {
        return bookSeats;
    }

    public File getFile() {
        return file;
    }

    public BookStatus getBookStatus() {
        return bookStatus;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BookScheduledTask getScheduledTask() {
        return scheduledTask;
    }
}
