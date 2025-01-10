package com.example.loginDemo.dto;

import com.example.loginDemo.domain.Item;
import com.example.loginDemo.domain.UserCustomItem;
import com.example.loginDemo.repository.UserCustomItemRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class OrderItemResponse {
    private Long orderItemId;
    private Item item;
    private UserCustomItem userCustomItem;
}
