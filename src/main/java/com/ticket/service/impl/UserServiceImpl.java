package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.common.RoleConstant;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
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



    @Override
    public UserAuthResponse auth(UserAuthRequest request) {
        String account = request.getAccount(); // 账号：用户名或邮箱
        String password = request.getPassword();

        // 1. 查用户（支持用户名/邮箱登录）
        // TODO: 如果这边用or的话，最好两个字段都用加上索引，不然会全表扫描，效率比较低，要么就是直接把邮箱作为用户名，用户名改成昵称，登陆只用邮箱作为账号
        User user = userMapper.selectByUsernameOrEmail(account, account);
        if (user == null) {
            // 2. 自动注册（默认角色USER，状态启用）
            user = new User();
            user.setUsername(account.contains("@") ? "user_" + System.currentTimeMillis() : account);//时间戳避免用户名重复，后续再改吧
            user.setEmail(account.contains("@") ? account : null); // 邮箱登录时自动填充email
            user.setPassword(password);
            user.setRole("USER"); // 默认普通用户
            user.setStatus(1);    // 默认启用
            // 无 request 场景，使用 userId 版本的审计方法，此处创建人为空即可
            AuditUtil.setCreateAuditFields(user, (Long) null);
            userMapper.insert(user);
        } else {
            // 3. 验证密码和状态（管理端可禁用用户）
            if (!password.equals(user.getPassword())) {
                throw new BusinessException("密码错误");
            }
            if (user.getStatus() == 0) {
                throw new BusinessException("账号已禁用，请联系管理员");
            }
        }

        // 4. 生成包含角色的Token
        String token = jwtUtil.generateToken(user.getId().toString(), user.getRole());
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
        user.setPassword(null);//这里设置密码为null是为了避免将密码返回给前端，避免返回敏感信息，也可以之后我再加一个专门的DTO来返回
        return Result.success(user);
    }



    @Override
    //这里为了避免用户端修改其他用户的信息，在UserController里强制把Id设置成了当前用户的Id
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
        List<User> users = userMapper.selectAll();
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
    @Override
    public PageResult<User> getUsersByPage(String username, Integer status, PageRequest pageRequest) {
        // 1. 处理分页参数
        if (pageRequest.getPage() == null || pageRequest.getPage() < 1) {
            pageRequest.setPage(1);
        }
        if (pageRequest.getSize() == null || pageRequest.getSize() < 1) {
            pageRequest.setSize(10);
        }

        int offset = (pageRequest.getPage() - 1) * pageRequest.getSize();
        int size = pageRequest.getSize();

        // 2. 查询列表 + 总数
        // TODO：如果图方便，可以了解Mybatis的分页插件
        List<User> list = userMapper.selectByPage(username, status, offset, size);
        Long total = userMapper.countByPage(username, status);

        // 3. 脱敏：不返回密码
        list.forEach(u -> u.setPassword(null));

        return new PageResult<>(list, total, pageRequest);
    }

    @Override
    public Result<User> getUserDetailForAdmin(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @Override
    public Result<String> updateUserStatus(Long id, Integer status) {
        // 参数校验
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态非法，必须是 0 或 1");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int rows = userMapper.updateStatus(id, status);
        if (rows <= 0) {
            return Result.error("更新用户状态失败");
        }
        return Result.success("更新用户状态成功");
    }

}


