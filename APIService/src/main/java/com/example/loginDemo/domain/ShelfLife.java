package com.example.loginDemo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;

@Entity
@Table(name = "shelf_lifes")
@NoArgsConstructor
@Getter @Setter
public class ShelfLife {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelf_life_id", updatable = false)
    private Long id;

    @Column(name = "sell_by_days", nullable = false)
    private int sellByDays;  // 유통기한 일수

    @Column(name = "use_by_days", nullable = false)
    private int useByDays;   // 소비기한 일수

    public ShelfLife(int sellByDays, int useByDays) {
        this.sellByDays = sellByDays;
        this.useByDays = useByDays;
    }

}
