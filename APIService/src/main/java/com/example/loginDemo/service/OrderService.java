package com.example.loginDemo.service;

import com.example.loginDemo.auth.JwtService;
import com.example.loginDemo.domain.*;
import com.example.loginDemo.dto.*;
import com.example.loginDemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final UserCustomItemRepository userCustomItemRepository;
    private final JwtService jwtService;

    //영수증으로 주문 추가
    @Transactional
    public void createOrder(OrderRequest orderRequest, String accessToken) {
        User user = getCurrentUser(accessToken);

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

            // 사전에 정의된 Item이 있는지 확인
            Optional<Item> existingItem = itemRepository.findByItemName(orderItemRequest.getItemName());

            if (existingItem.isPresent()) {
                // 정의된 Item이 있으면 해당 Item을 주문 항목에 설정
                orderItem.setItem(existingItem.get());
                orderItemRepository.save(orderItem);
            } else {
                // 정의되지 않은 Item은 주문 항목에서 제외
                System.out.println("Item not found: " + orderItemRequest.getItemName() + " (Skipping this item)");
            }
        }
    }

    //직접 주문 추가
    @Transactional
    public void createOrder2(OrderRequest2 orderRequest, String accessToken) {
        // 현재 사용자 정보 추출
        System.out.println("Extracting current user from access token...");
        User user = getCurrentUser(accessToken);
        System.out.println("User found: " + user.getUsername());

        // 주문 생성
        System.out.println("Creating order for user: " + user.getUsername());
        Order order = new Order();
        order.setOrderDate(orderRequest.getOrderDate());
        order.setUser(user);

        // 주문 저장
        System.out.println("Saving order to the database...");
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order saved with ID: " + savedOrder.getId());

        // 주문 아이템 처리
        for (OrderItemRequest2 orderItemRequest : orderRequest.getOrderItems()) {
            System.out.println("Processing order item: " + orderItemRequest.getItemName());

            // 새 주문 항목 생성
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);  // 주문과 연결
            orderItem.setCount(orderItemRequest.getCount());  // 수량 설정
            System.out.println("Set order item count: " + orderItemRequest.getCount());

            // 상품이 미리 정의되어 있는지 확인
            Optional<Item> existingItem = itemRepository.findByItemName(orderItemRequest.getItemName());
            if (existingItem.isPresent()) {
                System.out.println("Item found in the database: " + existingItem.get().getItemName());
                // 미리 정의된 상품이 있으면 그 상품을 사용
                orderItem.setItem(existingItem.get());
            } else {
                System.out.println("Item not found. Creating new UserCustomItem...");
                // 미리 정의된 상품이 없으면 UserCustomItem을 생성
                UserCustomItem userCustomItem = new UserCustomItem();
                userCustomItem.setItemName(orderItemRequest.getItemName());  // DTO에서 받은 itemName 설정
                userCustomItem.setCategory(orderItemRequest.getCategory());  // DTO에서 받은 category 설정
                userCustomItem.setStorageMethod(orderItemRequest.getStorageMethod());  // DTO에서 받은 storageMethod 설정
                userCustomItem.setSellByDays(orderItemRequest.getSellByDays());  // DTO에서 받은 sellByDays 설정
                userCustomItem.setUseByDays(orderItemRequest.getUseByDays());  // DTO에서 받은 useByDays 설정
                userCustomItem.setUser(user);  // 현재 사용자 설정

                // 새 UserCustomItem 저장
                userCustomItemRepository.save(userCustomItem);
                System.out.println("UserCustomItem created and saved: " + userCustomItem.getItemName());

                // 새 UserCustomItem을 OrderItem에 설정
                orderItem.setUserCustomItem(userCustomItem);
            }

            // 주문 항목 저장
            orderItemRepository.save(orderItem);
            System.out.println("Order item saved with ID: " + orderItem.getId());
        }

        System.out.println("Order creation process completed.");
    }

    // 유저별 식재료 조회
    public List<OrderItemResponse> findItemsByUser(String accessToken) {
        User user = getCurrentUser(accessToken);

        // 유저가 주문한 주문 아이템을 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrder_User(user);

        // 주문 아이템에 포함된 식재료 목록 반환
        return orderItems.stream()
                .map(orderItem -> new OrderItemResponse(orderItem.getId(), orderItem.getItem()))
                .distinct() // 중복된 식재료를 제거
                .collect(Collectors.toList());
    }

    // 유저가 주문한 주문 아이템 삭제
    @Transactional
    public void deleteOrderItemByUser(Long orderItemId, String accessToken) {
        User user = getCurrentUser(accessToken); // 중복된 유저 정보 추출 부분을 호출

        // 해당 유저의 주문에서 삭제하려는 주문 아이템 찾기
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .filter(item -> item.getOrder().getUser().equals(user)) // 해당 주문이 유저의 주문인지 확인
                .orElseThrow(() -> new IllegalArgumentException("OrderItem not found or not owned by the user"));

        // 주문 아이템 삭제
        orderItemRepository.delete(orderItem);
    }


    // 현재 로그인한 유저 정보 추출
    private User getCurrentUser(String accessToken) {
        String email = jwtService.extractUsername(accessToken);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
