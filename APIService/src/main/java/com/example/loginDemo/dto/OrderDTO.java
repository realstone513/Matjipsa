package com.example.loginDemo.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class OrderDTO {

    private Long userId;
    private LocalDate orderDate;
    private List<OrderItemDTO> orderItems;
}