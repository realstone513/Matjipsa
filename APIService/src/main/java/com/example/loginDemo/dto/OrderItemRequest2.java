package com.example.loginDemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest2 {
    private String itemName;
    private int count;
    private String category;
    private String storageMethod;
    private int sellByDays;
    private int useByDays;
}
