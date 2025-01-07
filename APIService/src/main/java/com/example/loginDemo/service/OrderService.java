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
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Map<String, String> createOrder(OrderRequest orderRequest) {
        // 주문을 생성
        Order order = new Order();
        order.setOrderDate(orderRequest.getOrderDate());

        // OrderItems 생성
        List<OrderItem> orderItems = orderRequest.getOrderItems().stream()
                .map(this::convertToOrderItem)
                .collect(Collectors.toList());

        // OrderItem들과 연결
        order.setOrderItems(orderItems);

        // 주문을 저장하고, 그 후 order_id가 자동으로 설정됨
        order = orderRepository.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrder(order);  // Order와 연결
        }

        orderItemRepository.saveAll(orderItems);

        return createResponse("Order created successfully!");
    }

    private Map<String, String> createResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    private OrderItem convertToOrderItem(OrderItemDTO orderItemDTO) {
        Optional<Item> optionalItem = itemRepository.findByItemName(orderItemDTO.getItemName());
        Item item = optionalItem.orElseThrow(() -> new RuntimeException("Item with name " + orderItemDTO.getItemName() + " not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(orderItemDTO.getCount());

        return orderItem;
    }

    // 모든 주문 조회
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
