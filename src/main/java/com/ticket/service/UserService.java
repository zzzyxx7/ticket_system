package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserService {

    Result<User> getCurrentUser(Long userId);

    UserAuthResponse auth(UserAuthRequest request);

    Result<String> updateUser(User user);

    Result<String> deleteUser(Long id);

    Result<User> getUserByUsername(String username);



    Result<List<User>> getAllUsers();

    Result<String> updateUserRole(Long id, String role);

    // 分页查询用户（管理端）
    PageResult<User> getUsersByPage(String username,
                                    Integer status,
                                    PageRequest pageRequest);

    // 查询单个用户详情（管理端）
    Result<User> getUserDetailForAdmin(Long id);

    // 启用/禁用用户（管理端）
    Result<String> updateUserStatus(Long id, Integer status);






}


