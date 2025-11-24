package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.Address;
import com.ticket.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user/address")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    // 获取当前用户的所有地址
    @GetMapping
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = Long.valueOf(userIdStr);
        List<Address> addresses = addressMapper.selectByUserId(userId);
        return Result.success(addresses);
    }

    // 添加收货地址
    @PostMapping
    public Result<String> addAddress(@RequestBody Address address, HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = Long.valueOf(userIdStr);

        address.setUserId(userId);

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }

        addressMapper.insert(address);
        return Result.success("地址添加成功");
    }

    // 修改收货地址
    @PutMapping("/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody Address address,
                                        HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = Long.valueOf(userIdStr);

        // 验证地址属于当前用户
        Address existingAddress = addressMapper.selectById(id);
        if (existingAddress == null || !existingAddress.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权修改");
        }

        address.setId(id);
        address.setUserId(userId);

        // 如果设置为默认地址，先取消其他默认地址
        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.setAllNonDefault(userId);
        }

        addressMapper.update(address);
        return Result.success("地址修改成功");
    }

    // 删除收货地址
    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = Long.valueOf(userIdStr);

        // 验证地址属于当前用户
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权删除");
        }

        addressMapper.deleteById(id);
        return Result.success("地址删除成功");
    }

    // 设置默认地址
    @PostMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        Long userId = Long.valueOf(userIdStr);

        // 验证地址属于当前用户
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在或无权操作");
        }

        // 先取消所有默认地址，再设置当前为默认
        addressMapper.setAllNonDefault(userId);
        addressMapper.setDefaultAddress(id, userId);

        return Result.success("默认地址设置成功");
    }
}