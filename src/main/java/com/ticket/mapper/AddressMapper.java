// src/main/java/com/ticket/mapper/AddressMapper.java
package com.ticket.mapper;

import com.ticket.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    // 基础CRUD
    Address selectById(Long id);
    List<Address> selectByUserId(Long userId);
    int insert(Address address);
    int update(Address address);
    int deleteById(Long id);

    // 业务方法
    int setAllNonDefault(Long userId);
    int setDefaultAddress(@Param("id") Long id, @Param("userId") Long userId);
    Address selectDefaultAddress(Long userId);
}