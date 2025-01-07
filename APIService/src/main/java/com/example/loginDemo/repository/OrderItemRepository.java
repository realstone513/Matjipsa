package com.example.loginDemo.repository;

import com.example.loginDemo.domain.Order;
import com.example.loginDemo.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}

