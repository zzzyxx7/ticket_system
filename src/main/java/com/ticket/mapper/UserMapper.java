package com.ticket.mapper;

import com.ticket.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByUsername(String username);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
    User selectByUsernameOrEmail(@Param("username") String username, @Param("email") String email);
    List<User> selectAdmins();
    List<User> selectAll();
    // 新增：管理端分页查询用户
    // 管理端分页查询用户
    List<User> selectByPage(
            @Param("username") String username,  // 模糊搜索
            @Param("status") Integer status,     // 启用/禁用
            @Param("offset") int offset,
            @Param("size") int size
    );

    Long countByPage(@Param("username") String username,
                     @Param("status") Integer status);

    // 修改用户状态（启用/禁用）
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status);
}
