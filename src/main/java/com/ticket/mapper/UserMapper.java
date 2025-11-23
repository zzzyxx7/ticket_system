package com.ticket.mapper;

import com.ticket.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User selectById(Long id);
    User selectByUsername(String username);
    int insert(User user);
    int update(User user);
    int deleteById(Long id);
}