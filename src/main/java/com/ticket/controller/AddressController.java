package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.Address;
import com.ticket.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    // 获取当前用户的所有地址
    @GetMapping
    public Result<List<Address>> getAddressList(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return addressService.getAddressList(userId);
    }

    // 添加收货地址
    @PostMapping
    public Result<String> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return addressService.addAddress(address, userId);
    }

    // 修改收货地址
    @PutMapping("/{id}")
    public Result<String> updateAddress(@PathVariable Long id, @RequestBody Address address,
                                        HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return addressService.updateAddress(id, address, userId, request);
    }

    // 删除收货地址
    @DeleteMapping("/{id}")
    public Result<String> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return addressService.deleteAddress(id, userId);
    }

    // 设置默认地址
    @PostMapping("/{id}/default")
    public Result<String> setDefaultAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return addressService.setDefaultAddress(id, userId);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        return userIdStr == null ? null : Long.valueOf(userIdStr);
    }
}