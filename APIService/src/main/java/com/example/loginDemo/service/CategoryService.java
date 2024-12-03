package com.example.loginDemo.service;

import com.example.loginDemo.domain.Category;
import com.example.loginDemo.domain.Item;
import com.example.loginDemo.repository.CategoryRepository;
import com.example.loginDemo.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    // 카테고리 생성
    public Category createCategory(String categoryName) {
        Category category = new Category(categoryName);
        return categoryRepository.save(category);
    }

    // 카테고리 이름으로 조회
    public Category findByCategoryName(String categoryName) {
        // Optional에서 값을 꺼내고, 없을 경우 예외 처리
        return categoryRepository.findByCategoryName(categoryName)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
    }

    // 모든 카테고리 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    //삭제
    public void deleteCategory(Long id) {
        itemRepository.deleteByCategoryId(id);
        categoryRepository.deleteById(id);
    }

}
