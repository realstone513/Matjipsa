package com.example.loginDemo.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@NoArgsConstructor
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", updatable = false)
    private Long id;

    @Column(nullable = true)
    private int count;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = true)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_custom_item_id", nullable = true)
    private UserCustomItem userCustomItem;
}
