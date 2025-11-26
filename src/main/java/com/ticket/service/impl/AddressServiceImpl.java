package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.entity.Address;
import com.ticket.mapper.AddressMapper;
import com.ticket.service.AddressService;
import com.ticket.util.AuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public Result<List<Address>> getAddressList(Long userId) {
        List<Address> addresses = addressMapper.selectByUserId(userId);
        return Result.success(addresses);
    }

    @Override
    public Result<String> addAddress(Address address, Long userId) {
        address.setUserId(userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }
        addressMapper.insert(address);
        return Result.success("地址添加成功");
    }

    @Override
    public Result<String> updateAddress(Long id, Address address, Long userId, HttpServletRequest request) {
        Address existingAddress = addressMapper.selectById(id);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权修改");
        }
        address.setId(id);
        address.setUserId(userId);
        AuditUtil.setUpdateAuditFields(address, request);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }
        addressMapper.update(address);
        return Result.success("地址修改成功");
    }

    @Override
    public Result<String> deleteAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权删除");
        }
        addressMapper.deleteById(id);
        return Result.success("地址删除成功");
    }

    @Override
    public Result<String> setDefaultAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权操作");
        }
        addressMapper.setAllNonDefault(userId);
        addressMapper.setDefaultAddress(id, userId);
        return Result.success("默认地址设置成功");
    }
}


