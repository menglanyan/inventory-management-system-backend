package com.github.menglanyan.inventory_management.services.impl;

import com.github.menglanyan.inventory_management.dtos.LoginRequest;
import com.github.menglanyan.inventory_management.dtos.RegisterRequest;
import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.entities.User;
import com.github.menglanyan.inventory_management.enums.UserRole;
import com.github.menglanyan.inventory_management.exceptions.InvalidCredentialsException;
import com.github.menglanyan.inventory_management.exceptions.NotFoundException;
import com.github.menglanyan.inventory_management.repositories.UserRepository;
import com.github.menglanyan.inventory_management.security.JwtUtils;
import com.github.menglanyan.inventory_management.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;

    @Override
    public Response registerUser(RegisterRequest registerRequest) {

        UserRole role = UserRole.MANAGER;

        if (registerRequest.getRole() != null) {
            role = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(userToSave);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User was successfully registered")
                .build();
    }

    @Override
    public Response loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Email Not Found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Password Does Not Match");
        }

        String token = jwtUtils.generateToken(user.getEmail());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Login Successful")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 months")
                .build();
    }
}
