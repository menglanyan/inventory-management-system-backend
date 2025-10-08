package com.github.menglanyan.inventory_management.services.impl;

import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.dtos.TransactionDTO;
import com.github.menglanyan.inventory_management.dtos.TransactionRequest;
import com.github.menglanyan.inventory_management.entities.Product;
import com.github.menglanyan.inventory_management.entities.Supplier;
import com.github.menglanyan.inventory_management.entities.Transaction;
import com.github.menglanyan.inventory_management.entities.User;
import com.github.menglanyan.inventory_management.enums.TransactionStatus;
import com.github.menglanyan.inventory_management.enums.TransactionType;
import com.github.menglanyan.inventory_management.exceptions.NameValueRequiredException;
import com.github.menglanyan.inventory_management.exceptions.NotFoundException;
import com.github.menglanyan.inventory_management.repositories.ProductRepository;
import com.github.menglanyan.inventory_management.repositories.SupplierRepository;
import com.github.menglanyan.inventory_management.repositories.TransactionRepository;
import com.github.menglanyan.inventory_management.services.TransactionService;
import com.github.menglanyan.inventory_management.services.UserService;
import com.github.menglanyan.inventory_management.specification.TransactionFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final ProductRepository productRepository;

    private final SupplierRepository supplierRepository;

    private final UserService userService;

    private final ModelMapper modelMapper;


    @Override
    public Response purchase(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();

        Long supplierId = transactionRequest.getSupplierId();

        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) {
            throw new NameValueRequiredException("Supplier id is required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        User user = userService.getCurrentLoggedInUser();

        // Update product stock quantity and save it
        product.setStockQuantity(product.getStockQuantity() + quantity);

        productRepository.save(product);

        // Create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .supplier(supplier)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Purchase Made Successfully")
                .build();

    }

    @Override
    public Response sell(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();

        Integer quantity = transactionRequest.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        User user = userService.getCurrentLoggedInUser();

        // Update product stock quantity and save it
        product.setStockQuantity(product.getStockQuantity() - quantity);

        productRepository.save(product);

        // Create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Sale Made Successfully")
                .build();

    }

    @Override
    public Response returnToSupplier(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();

        Long supplierId = transactionRequest.getSupplierId();

        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) {
            throw new NameValueRequiredException("Supplier id is required");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product Not Found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier Not Found"));

        User user = userService.getCurrentLoggedInUser();

        // Update product stock quantity and save it
        product.setStockQuantity(product.getStockQuantity() - quantity);

        productRepository.save(product);

        // Create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)     // The status is COMPLETED when supplier confirms
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .note(transactionRequest.getNote())
                .build();

        transactionRepository.save(transaction);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Product Returned In Progress")
                .build();

    }

    @Override
    public Response getAllTransactions(int page, int size, String searchValue) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Specification<Transaction> spec = TransactionFilter.byFliter(searchValue);

        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransactionDTO> transactionDTOList = modelMapper.map(transactionPage.getContent(),
                new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOList.forEach(transactionDTO -> {
                    transactionDTO.setSupplier(null);
                    transactionDTO.setUser(null);
                    transactionDTO.setProduct(null);
                });

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions Retrieved Successfully")
                .transactions(transactionDTOList)
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .build();

    }

    @Override
    public Response getTransactionById(Long id) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);

        transactionDTO.setUser(null);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transaction Retrieved Successfully")
                .transaction(transactionDTO)
                .build();
    }

    @Override
    public Response getTransactionsByMonthAndYear(int month, int year) {

        List<Transaction> transactions = transactionRepository.findAll(TransactionFilter.byMonthAndYear(month, year));

        List<TransactionDTO> transactionDTOList = modelMapper.map(transactions, new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOList.forEach(transactionDTO -> {
            transactionDTO.setSupplier(null);
            transactionDTO.setUser(null);
            transactionDTO.setProduct(null);
        });

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transactions Retrieved Successfully")
                .transactions(transactionDTOList)
                .build();

    }

    @Override
    public Response updateTransactionStatus(Long transactionId, TransactionStatus transactionStatus) {

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));

        existingTransaction.setStatus(transactionStatus);

        existingTransaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(existingTransaction);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Transaction Status Updated Successfully")
                .build();

    }

}
