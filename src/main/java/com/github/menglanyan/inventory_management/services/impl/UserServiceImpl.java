package com.github.menglanyan.inventory_management.services.impl;

import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.dtos.UserDTO;
import com.github.menglanyan.inventory_management.entities.User;
import com.github.menglanyan.inventory_management.exceptions.NotFoundException;
import com.github.menglanyan.inventory_management.repositories.UserRepository;
import com.github.menglanyan.inventory_management.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ModelMapper modelMapper;

    @Override
    public Response getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // Break the bidirectional relationship to avoid recursive loop during serialization:
        users.forEach(user -> user.setTransactions(null));

        List<UserDTO> userDTOs = modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Users Retrieved Successfully")
                .users(userDTOs)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        // Break the bidirectional relationship to avoid recursive loop during serialization:
        user.setTransactions(null);

        return user;
    }

    @Override
    public Response getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        // Break the bidirectional relationship to avoid recursive loop during serialization:
        userDTO.setTransactions(null);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Retrieved Successfully")
                .user(userDTO)
                .build();

    }

    @Override
    public Response updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            existingUser.setEmail(userDTO.getEmail());
        }

        if (userDTO.getPhoneNumber() != null && !userDTO.getPhoneNumber().isBlank()) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }

        if (userDTO.getName() != null && !userDTO.getName().isBlank()) {
            existingUser.setName(userDTO.getName());
        }

        if (userDTO.getPassword() != null && userDTO.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRole() != null) {
            existingUser.setRole(userDTO.getRole());
        }

        userRepository.save(existingUser);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Updated Successfully")
                .build();
    }

    @Override
    public Response deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        userRepository.deleteById(id);

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User Deleted Successfully")
                .build();
    }

    @Override
    public Response getUserTransactions(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User Not Found"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.getTransactions().forEach(transactionDTO -> {
            // Break the bidirectional relationship to avoid recursive loop during serialization:
            transactionDTO.setUser(null);
            transactionDTO.setSupplier(null);
        });

        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("User's Transactions Retrieved Successfully")
                .user(userDTO)
                .build();
    }
}
