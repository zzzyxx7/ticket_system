package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.*;
import com.ticket.entity.User;

import java.util.List;

public interface UserService {

    Result<UserDTO> getCurrentUser(Long userId);

    UserAuthResponse auth(UserAuthRequest request);

    Result<String> updateUser(UserUpdateDTO dto);

    // 管理端更新用户（可修改所有字段，包括 role、status）
    Result<String> updateUserForAdmin(User user);

    Result<String> deleteUser(Long id);

    Result<User> getUserByUsername(String username);

    Result<List<User>> getAllUsers();

    // 分页查询用户（管理端）
    PageResult<User> getUsersByPage(String username,
                                    Integer status,
                                    PageRequest pageRequest);

    // 查询单个用户详情（管理端）
    Result<User> getUserDetailForAdmin(Long id);

    // 启用/禁用用户（管理端）
    Result<String> updateUserStatus(Long id, Integer status);



}


