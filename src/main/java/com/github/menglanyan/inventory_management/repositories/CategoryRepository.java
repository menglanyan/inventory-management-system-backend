package com.github.menglanyan.inventory_management.repositories;

import com.github.menglanyan.inventory_management.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
