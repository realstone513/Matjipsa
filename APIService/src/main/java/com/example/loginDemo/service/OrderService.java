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

    // 주문 생성
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

    // 직접 주문 추가
    @Transactional
    public void createOrder2(OrderRequest2 orderRequest, String accessToken) {
        User user = getCurrentUser(accessToken);

        // 주문 생성
        Order order = new Order();
        order.setOrderDate(orderRequest.getOrderDate());
        order.setUser(user);

        Order savedOrder = orderRepository.save(order);

        // 주문 아이템 처리
        for (OrderItemRequest2 orderItemRequest : orderRequest.getOrderItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setCount(orderItemRequest.getCount());

            // 먼저 기존 아이템을 확인
            Optional<Item> existingItem = itemRepository.findByItemName(orderItemRequest.getItemName());

            if (existingItem.isPresent()) {
                // 기존 아이템이 존재하면 해당 아이템을 사용
                orderItem.setItem(existingItem.get());
            } else {
                // 기존 아이템이 없다면, UserCustomItem으로 저장
                UserCustomItem customItem = new UserCustomItem();
                customItem.setItemName(orderItemRequest.getItemName());
                customItem.setCount(orderItemRequest.getCount());
                customItem.setCategory(orderItemRequest.getCategory());
                customItem.setStorageMethod(orderItemRequest.getStorageMethod());
                customItem.setSellByDays(orderItemRequest.getSellByDays());
                customItem.setUseByDays(orderItemRequest.getUseByDays());
                customItem.setUser(user);

                UserCustomItem savedCustomItem = userCustomItemRepository.save(customItem);
                orderItem.setUserCustomItem(savedCustomItem);
            }

            orderItemRepository.save(orderItem);
        }
    }

    // 유저별 식재료 조회
    public List<OrderItemResponse> findItemsByUser(String accessToken) {
        User user = getCurrentUser(accessToken);

        // 유저가 주문한 주문 아이템을 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrder_User(user);

        // 주문 아이템에 포함된 식재료 목록 반환
        return orderItems.stream()
                .map(orderItem -> {
                    // 기존 아이템이 있을 경우, 해당 아이템을 사용
                    if (orderItem.getItem() != null) {
                        return new OrderItemResponse(orderItem.getId(), orderItem.getItem(), null); // Item만 포함
                    }
                    // 사용자 정의 아이템이 있을 경우, 해당 사용자 정의 아이템을 사용
                    else if (orderItem.getUserCustomItem() != null) {
                        return new OrderItemResponse(orderItem.getId(), null, orderItem.getUserCustomItem()); // UserCustomItem만 포함
                    }
                    return null; // 이 부분은 안전성 확보를 위해 추가 (null 반환 방지)
                })
                .filter(item -> item != null) // null 값 필터링
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
