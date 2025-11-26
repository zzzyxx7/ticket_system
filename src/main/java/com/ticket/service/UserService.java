package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;
import com.ticket.entity.User;

public interface UserService {

    Result<User> getCurrentUser(Long userId);

    Result<String> addUser(User user);

    Result<String> updateUser(User user);

    Result<String> deleteUser(Long id);

    Result<User> getUserByUsername(String username);

    Result<LoginResponse> login(LoginRequest request);
}


