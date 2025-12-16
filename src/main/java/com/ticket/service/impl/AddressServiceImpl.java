package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.AddressDTO;
import com.ticket.entity.Address;
import com.ticket.mapper.AddressMapper;
import com.ticket.service.AddressService;
import com.ticket.util.AddressConvertor;
import com.ticket.util.AuditUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//所有关于增删改的操作，都要带上事务注解@Transactional(已完成)
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;
    @Autowired
    private AddressConvertor addressConvertor;

    @Override
    public Result<List<AddressDTO>> getAddressList(Long userId) {
        List<Address> addresses = addressMapper.selectByUserId(userId);
        List<AddressDTO> dtoList = addressConvertor.toDTOList(addresses);
        return Result.success(dtoList);
    }

    @Override
    @Transactional
    public Result<String> addAddress(Address address, Long userId) {
        address.setUserId(userId);
        AuditUtil.setCreateAuditFields(address, userId);  // 调用工具类设置createdTime等
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }
        addressMapper.insert(address);
        return Result.success("地址添加成功");
    }

    @Override
    @Transactional
    // request 没必要(已完成)
    public Result<String> updateAddress(Long id, Address address, Long userId) {
        Address existingAddress = addressMapper.selectById(id);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权修改");
        }
        address.setId(id);
        address.setUserId(userId);
        // 直接使用传入的 userId，而不是从 request 获取
        AuditUtil.setUpdateAuditFields(address, userId);
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }
        addressMapper.update(address);
        return Result.success("地址修改成功");
    }

    @Override
    @Transactional
    public Result<String> deleteAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权删除");
        }
        addressMapper.deleteById(id);
        return Result.success("地址删除成功");
    }

    @Override
    @Transactional
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


