package com.github.menglanyan.inventory_management.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.menglanyan.inventory_management.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {

    // Generic
    private int statusCode;

    private String message;

    // For login
    private String token;

    private UserRole role;

    private String expirationTime;

    // For pagination
    private Integer totalPages;

    private Long totalElements;

    // Data output optional
    private UserDTO user;
    private List<UserDTO> users;

    private SupplierDTO supplier;
    private List<SupplierDTO> suppliers;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private ProductDTO product;
    private List<ProductDTO> products;

    private TransactionDTO transaction;
    private List<TransactionDTO> transactions;

    private final LocalDateTime timestamp = LocalDateTime.now();

}
