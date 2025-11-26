package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.TicketOrder;
import com.ticket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ticket.dto.CreateOrderRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {
        String userIdStr = (String) httpRequest.getAttribute("userId");
        if (userIdStr == null) {
            return Result.error("用户未登录");
        }
        return orderService.createOrder(request, Long.valueOf(userIdStr));
    }

    // 获取当前用户的所有订单
    @GetMapping("/list")
    public Result<List<TicketOrder>> getOrderList(HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.getOrderList(userId);
    }

    // 根据订单ID获取订单详情
    @GetMapping("/{id}")
    public Result<TicketOrder> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    // 取消订单
    @PostMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.cancelOrder(id, userId,request);
    }

    // 分页查询订单列表
    @GetMapping("/page")
    public Result<List<TicketOrder>> getOrdersByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.getOrdersByPage(userId, page, size);
    }

    // 更新订单
    @PutMapping("/{id}")
    public Result<String> updateOrder(@PathVariable Long id, @RequestBody TicketOrder order, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.updateOrder(id, order, userId);
    }

    // 删除订单
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.deleteOrder(id, userId);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        return userIdStr == null ? null : Long.valueOf(userIdStr);
    }

}