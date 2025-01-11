package com.example.loginDemo.dto;

import com.example.loginDemo.domain.Item;
import com.example.loginDemo.domain.UserCustomItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter @Setter
public class OrderItemResponse {
    private Long orderItemId;
    private LocalDate orderDate;
    private String itemName;
    private String categoryName;
    private String storageMethodName;
    private Integer sellByDays;
    private Integer useByDays;

}
