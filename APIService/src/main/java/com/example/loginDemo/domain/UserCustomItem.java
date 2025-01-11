package com.example.loginDemo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class UserCustomItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private int count;
    private String categoryName;
    private String storageMethodName;
    private int sellByDays;
    private int useByDays;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
