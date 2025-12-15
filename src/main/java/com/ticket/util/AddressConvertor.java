package com.ticket.util;

import com.ticket.dto.AddressDTO;


import com.ticket.entity.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
public class AddressConvertor {
    // 单个实体转DTO
    public AddressDTO toDTO(Address address) {
        if (address == null) {
            return null;
        }
        AddressDTO dto = new AddressDTO();
        // 复制核心业务字段（不包含审计字段）
        dto.setId(dto.getId());
        dto.setUserId(dto.getUserId());
        dto.setRecipientName(dto.getRecipientName());
        dto.setPhone(dto.getPhone());
        dto.setAddressDetail(dto.getAddressDetail());
        dto.setIsDefault(dto.getIsDefault());
        return dto;
    }

    // 批量实体转DTO列表
    public List<AddressDTO> toDTOList(List<Address> addresses) {
        return addresses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}
