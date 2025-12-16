package com.ticket.util;

import com.ticket.dto.UserDTO;
import com.ticket.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class UserConvertor {
    // 单个实体转DTO
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        // 复制核心业务字段（不包含审计字段）
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        return dto;
    }


    // 批量实体转DTO列表
    public List<UserDTO> toDTOList(List<User> Users) {
        return Users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
