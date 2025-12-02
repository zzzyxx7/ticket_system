package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.common.RoleConstant;
import com.ticket.dto.UserAuthRequest;
import com.ticket.dto.UserAuthResponse;
import com.ticket.entity.User;
import com.ticket.exception.BusinessException;
import com.ticket.mapper.UserMapper;
import com.ticket.service.UserService;
import com.ticket.util.AuditUtil;
import com.ticket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;


    public UserAuthResponse auth(UserAuthRequest request) {
        String account = request.getAccount();
        String password = request.getPassword();

        // 1. 先查用户：用account匹配用户名或邮箱
        User user = userMapper.selectByUsernameOrEmail(account, account);
        if (user == null) {
            // 2. 用户不存在 → 自动注册
            user = new User();
            user.setUsername(account.contains("@") ? "user_" + System.currentTimeMillis() : account); // 邮箱登录时生成默认用户名
            user.setEmail(account.contains("@") ? account : null); // 若account是邮箱，赋值email
            user.setPassword(password);
            AuditUtil.setCreateAuditFields(user,(Long) null); // 注册时无登录用户，createdBy可设为null或默认值
            userMapper.insert(user);
        } else {
            // 3. 用户存在 → 验证密码
            if (!password.equals(user.getPassword())) {
                throw new BusinessException("密码错误");
            }
        }

        // 4. 生成Token返回
        String token = jwtUtil.generateToken(user.getId().toString());
        UserAuthResponse response = new UserAuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        return response;
    }
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
    public Result<String> updateUser(User user) {
        // 1. 校验id是否为空
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        // 2. 检查用户是否存在
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            return Result.error("用户不存在，无法更新");
        }
        // 3. 执行更新并检查影响行数
        int rowsAffected = userMapper.update(user);
        if (rowsAffected <= 0) {
            return Result.error("用户更新失败，未找到匹配记录或数据未变更");
        }
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
    public Result<List<User>> getAllUsers() {
        List<User> users = userMapper.selectAll(); // 需要在UserMapper中新增selectAll方法
        users.forEach(user -> user.setPassword(null)); // 隐藏密码
        return Result.success(users);
    }

    @Override
    public Result<String> updateUserRole(Long id, String role) {
        // 校验角色合法性
        if (!RoleConstant.USER.equals(role) && !RoleConstant.ADMIN.equals(role)) {
            return Result.error("角色必须是USER或ADMIN");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setRole(role);
        userMapper.update(user);
        return Result.success("角色更新成功");
    }


}


