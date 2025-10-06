package com.github.menglanyan.inventory_management.services;

import com.github.menglanyan.inventory_management.dtos.LoginRequest;
import com.github.menglanyan.inventory_management.dtos.RegisterRequest;
import com.github.menglanyan.inventory_management.dtos.Response;

public interface AuthService {

    Response registerUser(RegisterRequest registerRequest);

    Response loginUser(LoginRequest loginRequest);

}
