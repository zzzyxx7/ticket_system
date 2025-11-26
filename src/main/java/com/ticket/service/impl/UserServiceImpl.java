package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import com.ticket.service.UserService;
import com.ticket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<User> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<String> addUser(User user) {
        userMapper.insert(user);
        return Result.success("用户添加成功");
    }

    @Override
    public Result<String> updateUser(User user) {
        userMapper.update(user);
        return Result.success("用户更新成功");
    }

    @Override
    public Result<String> deleteUser(Long id) {
        userMapper.deleteById(id);
        return Result.success("用户删除成功");
    }

    @Override
    public Result<User> getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<LoginResponse> login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名不存在");
        }
        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(user.getId().toString());

        LoginResponse response = new LoginResponse();
        response.setToken(token);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        response.setUserInfo(userInfo);

        return Result.success(response);
    }
}


