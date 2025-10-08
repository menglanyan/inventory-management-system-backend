package com.github.menglanyan.inventory_management.services;

import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.dtos.TransactionRequest;
import com.github.menglanyan.inventory_management.enums.TransactionStatus;

public interface TransactionService {

    Response purchase(TransactionRequest transactionRequest);

    Response sell(TransactionRequest transactionRequest);

    Response returnToSupplier(TransactionRequest transactionRequest);

    Response getAllTransactions(int page, int size, String searchValue);

    Response getTransactionById(Long id);

    Response getTransactionsByMonthAndYear(int month, int year);

    Response updateTransactionStatus(Long transactionId, TransactionStatus transactionStatus);

}
