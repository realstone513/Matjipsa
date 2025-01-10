package com.example.loginDemo.repository;

import com.example.loginDemo.domain.UserCustomItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCustomItemRepository extends JpaRepository<UserCustomItem, Long> {
}
