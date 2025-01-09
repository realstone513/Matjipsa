package com.example.loginDemo.controller;

import com.example.loginDemo.domain.Item;
import com.example.loginDemo.repository.OrderItemRepository;
import com.example.loginDemo.repository.OrderRepository;
import com.example.loginDemo.service.OrderService;
import com.example.loginDemo.dto.*;
import com.example.loginDemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String accessToken) {
        // Authorization 헤더에서 "Bearer " 제거하고 토큰만 추출
        String token = accessToken.replace("Bearer ", "");

        orderService.createOrder(orderRequest, token);

        // 성공적인 응답 반환
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    // 유저별 식재료 조회
    @GetMapping("/user_item")
    public List<Item> getUserItems(@RequestHeader("Authorization") String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        return orderService.findItemsByUser(token);
    }
}
