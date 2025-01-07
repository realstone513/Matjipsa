package com.example.loginDemo.service;

import com.example.loginDemo.domain.Item;
import com.example.loginDemo.domain.Order;
import com.example.loginDemo.domain.OrderItem;
import com.example.loginDemo.domain.User;
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
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void saveOrder(OrderDTO orderDTO) {
        // 사용자 ID로 User 객체를 조회
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + orderDTO.getUserId()));

        // 주문 생성
        Order order = new Order();
        order.setOrderDate(orderDTO.getOrderDate());
        order.setUser(user); // User 객체 설정

        orderRepository.save(order);

        orderDTO.getOrderItems().forEach(orderItemDTO -> {
            Item item = itemRepository.findByItemName(orderItemDTO.getItemName())
                    .orElseThrow(() -> new IllegalArgumentException("Item not found: " + orderItemDTO.getItemName()));

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setCount(orderItemDTO.getCount());
            orderItem.setOrder(order);
            orderItemRepository.save(orderItem);
        });
    }

    public List<OrderDTO> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(order -> {
            // 주문 항목 리스트 생성
            List<OrderItemDTO> orderItemDTOs = orderItemRepository.findByOrder(order).stream().map(orderItem -> {
                OrderItemDTO orderItemDTO = new OrderItemDTO();
                orderItemDTO.setItemName(orderItem.getItem().getItemName());
                orderItemDTO.setCount(orderItem.getCount());
                return orderItemDTO;
            }).collect(Collectors.toList());

            // OrderDTO 생성
            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderDate(order.getOrderDate());
            orderDTO.setOrderItems(orderItemDTOs);
            orderDTO.setUserId(order.getUser().getId());  // 사용자 ID 설정
            return orderDTO;
        }).collect(Collectors.toList());
    }
}
