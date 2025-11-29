package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.entity.User;

public interface UserService {

    Result<User> getCurrentUser(Long userId);

    UserAuthResponse auth(UserAuthRequest request);

    Result<String> updateUser(User user);

    Result<String> deleteUser(Long id);

    Result<User> getUserByUsername(String username);


}


