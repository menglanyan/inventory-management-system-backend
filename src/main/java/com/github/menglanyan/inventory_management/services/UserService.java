package com.github.menglanyan.inventory_management.services;

import com.github.menglanyan.inventory_management.dtos.Response;
import com.github.menglanyan.inventory_management.dtos.UserDTO;
import com.github.menglanyan.inventory_management.entities.User;

public interface UserService {

    Response getAllUsers();

    User getCurrentLoggedInUser();

    Response getUserById(Long id);

    Response updateUser(Long id, UserDTO userDTO);

    Response deleteUser(Long id);

    Response getUserTransactions(Long id);

}
