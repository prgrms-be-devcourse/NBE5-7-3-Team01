package com.fifo.ticketing.domain.performance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "grades")
@NoArgsConstructor
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", foreignKey = @ForeignKey(name = "fk_grade_to_place"))
    private Place place;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private Integer seatCount;

    @Column(nullable = false)
    private Integer defaultPrice;

    public Long getId() {
        return id;
    }

    public Place getPlace() {
        return place;
    }

    public String getGrade() {
        return grade;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public Integer getDefaultPrice() {
        return defaultPrice;
    }
}
