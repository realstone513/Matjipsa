package com.example.loginDemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    private String itemName;
    private int count;
}
