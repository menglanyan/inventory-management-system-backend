package com.github.menglanyan.inventory_management.services;

import com.github.menglanyan.inventory_management.dtos.CategoryDTO;
import com.github.menglanyan.inventory_management.dtos.Response;

public interface CategoryService {

    Response createCategory(CategoryDTO categoryDTO);

    Response getAllCategories();

    Response getCategoryById(Long id);

    Response updateCategory(Long id, CategoryDTO categoryDTO);

    Response deleteCategory(Long id);
}
