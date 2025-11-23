package com.ticket.controller;
import com.ticket.common.Result;
import com.ticket.dto.LoginRequest;
import com.ticket.dto.LoginResponse;
import com.ticket.entity.User;
import com.ticket.mapper.UserMapper;
import com.ticket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        return Result.success(user);
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        userMapper.insert(user);
        return Result.success("用户添加成功");
    }
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        userMapper.update(user);
        return Result.success("用户更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success("用户删除成功");
    }

    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userMapper.selectByUsername(username);
        return Result.success(user);
    }


    @Autowired
    private JwtUtil jwtUtil;
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 验证用户名和密码
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名不存在");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return Result.error("密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId().toString());

        // 构建响应
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