package com.github.menglanyan.inventory_management.services;

import com.github.menglanyan.inventory_management.dtos.ProductDTO;
import com.github.menglanyan.inventory_management.dtos.Response;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

    Response saveProduct(ProductDTO productDTO, MultipartFile imageFile);

    Response updateProduct(ProductDTO productDTO, MultipartFile imageFile);

    Response getAllProducts();

    Response getProductById(Long id);

    Response deleteProduct(Long id);

    Response searchProduct(String input);

}
