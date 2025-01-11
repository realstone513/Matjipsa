package com.example.loginDemo.service;

import com.example.loginDemo.auth.JwtService;
import com.example.loginDemo.domain.*;
import com.example.loginDemo.dto.*;
import com.example.loginDemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
                customItem.setCategoryName(orderItemRequest.getCategory());
                customItem.setStorageMethodName(orderItemRequest.getStorageMethod());
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
                    LocalDate orderDate = orderItem.getOrder().getOrderDate(); // 주문 날짜 가져오기
                    if (orderItem.getItem() != null) {
                        // 기존 Item 정보를 OrderItemResponse로 매핑
                        Item item = orderItem.getItem();
                        return new OrderItemResponse(
                                orderItem.getId(),
                                orderDate, // 주문 날짜 설정
                                item.getItemName(),
                                item.getCategory().getCategoryName(),
                                item.getStorageMethod().getStorageMethodName(),
                                item.getShelfLife().getSellByDays(),
                                item.getShelfLife().getUseByDays()
                        );
                    } else if (orderItem.getUserCustomItem() != null) {
                        // UserCustomItem 정보를 OrderItemResponse로 매핑
                        UserCustomItem customItem = orderItem.getUserCustomItem();
                        return new OrderItemResponse(
                                orderItem.getId(),
                                orderDate, // 주문 날짜 설정
                                customItem.getItemName(),
                                customItem.getCategoryName(),
                                customItem.getStorageMethodName(),
                                customItem.getSellByDays(),
                                customItem.getUseByDays()
                        );
                    }
                    return null; // 안전을 위해 추가 (null 반환 방지)
                })
                .filter(item -> item != null) // null 값 필터링
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
