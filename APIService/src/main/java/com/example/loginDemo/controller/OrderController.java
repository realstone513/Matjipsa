package com.example.loginDemo.controller;

import com.example.loginDemo.service.OrderService;
import com.example.loginDemo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 영수증으로 주문 추가
    @PostMapping("/receipts")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);
        orderService.createOrder(orderRequest, token);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 직접 주문 추가
    @PostMapping("/manual")
    public ResponseEntity<String> createOrder3(@RequestBody OrderRequest2 orderRequest, @RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);
        orderService.createOrder2(orderRequest, token);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 유저별 식재료 조회
    @GetMapping
    public ResponseEntity<List<OrderItemResponse>> getUserItems(@RequestHeader("Authorization") String accessToken) {
        String token = extractToken(accessToken);
        return ResponseEntity.ok(orderService.findItemsByUser(token));
    }

    // 유저가 주문 아이템 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrderItemByUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String accessToken) {
        try {
            String token = extractToken(accessToken);
            orderService.deleteOrderItemByUser(id, token);
            return ResponseEntity.ok("Order item deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 토큰 추출 메서드
    private String extractToken(String accessToken) {
        return accessToken.replace("Bearer ", "");
    }
}
