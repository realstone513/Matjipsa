package com.example.loginDemo.service;

import com.example.loginDemo.domain.Item;
import com.example.loginDemo.domain.Order;
import com.example.loginDemo.domain.OrderItem;
import com.example.loginDemo.dto.*;
import com.example.loginDemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void saveOrder(OrderDTO orderDTO) {
        // 주문 생성
        Order order = new Order();
        order.setOrderDate(orderDTO.getOrderDate());

        // OrderItems 생성
        List<OrderItem> orderItems = orderDTO.getOrderItems().stream().map(orderItemDTO -> {
            Item item = itemRepository.findByItemName(orderItemDTO.getItemName())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + orderItemDTO.getItemName()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(orderItemDTO.getCount());
            orderItem.setOrder(order);
            return orderItem;
        }).toList();
        
        orderRepository.save(order);
    }
}
