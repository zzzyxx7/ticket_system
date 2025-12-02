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
}
