package com.example.loginDemo.service;

import com.example.loginDemo.auth.JwtService;
import com.example.loginDemo.domain.Item;
import com.example.loginDemo.domain.Order;
import com.example.loginDemo.domain.OrderItem;
import com.example.loginDemo.domain.User;
import com.example.loginDemo.dto.*;
import com.example.loginDemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final JwtService jwtService;

    // 주문 생성
    @Transactional
    public void createOrder(OrderRequest orderRequest, String accessToken) {
        User user = getCurrentUser(accessToken); // 중복된 유저 정보 추출 부분을 호출

        // 주문 생성
        Order order = new Order();
        order.setOrderDate(orderRequest.getOrderDate());
        order.setUser(user);

        Order savedOrder = orderRepository.save(order);

        // 주문 아이템 처리
        for (OrderItemRequest orderItemRequest : orderRequest.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setCount(orderItemRequest.getCount());

            itemRepository.findByItemName(orderItemRequest.getItemName())
                    .ifPresent(item -> {
                        orderItem.setItem(item);
                        orderItemRepository.save(orderItem);
                    });
        }
    }

    // 유저별 식재료 조회
    public List<Item> findItemsByUser(String accessToken) {
        User user = getCurrentUser(accessToken); // 중복된 유저 정보 추출 부분을 호출

        // 유저가 주문한 주문 아이템을 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrder_User(user);

        // 주문 아이템에 포함된 식재료 목록 반환
        return orderItems.stream()
                .map(OrderItem::getItem) // OrderItem에서 Item을 추출
                .distinct() // 중복된 식재료를 제거
                .collect(Collectors.toList());
    }

    // 현재 로그인한 유저 정보 추출
    private User getCurrentUser(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
