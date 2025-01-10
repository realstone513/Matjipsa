package com.example.loginDemo.dto;

import com.example.loginDemo.domain.Item;
<<<<<<< HEAD
import com.example.loginDemo.domain.UserCustomItem;
import com.example.loginDemo.repository.UserCustomItemRepository;
=======
>>>>>>> 569273604ceedf422aeb12459b5c8d6b539fa3e8
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class OrderItemResponse {
    private Long orderItemId;
    private Item item;
<<<<<<< HEAD
    private UserCustomItem userCustomItem;
=======

>>>>>>> 569273604ceedf422aeb12459b5c8d6b539fa3e8
}
