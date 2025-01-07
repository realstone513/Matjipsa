package com.example.loginDemo.controller;

import com.example.loginDemo.domain.Order;
import com.example.loginDemo.domain.OrderItem;
import com.example.loginDemo.domain.User;
import com.example.loginDemo.repository.OrderItemRepository;
import com.example.loginDemo.repository.OrderRepository;
import com.example.loginDemo.service.OrderService;
import com.example.loginDemo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            // 주문 생성
            orderService.saveOrder(orderDTO);

            // 생성된 주문을 응답으로 반환
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            // 예외 발생 시 오류 메시지 반환
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrders(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        // 로그인한 사용자 정보 가져오기
        User loggedInUser = (User) userDetails;

        // 로그인한 유저와 요청된 유저 ID가 일치하는지 확인
        if (!loggedInUser.getId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 권한 없음
        }

        // 유저의 주문 목록 조회
        List<Order> orders = orderRepository.findByUserId(userId);

        // 주문을 DTO로 변환
        List<OrderDTO> orderDTOs = orders.stream().map(order -> {
            // 주문 항목들(OrderItem)도 함께 DTO로 변환
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
            return orderDTO;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(orderDTOs, HttpStatus.OK); // 주문 목록 반환
    }



}
