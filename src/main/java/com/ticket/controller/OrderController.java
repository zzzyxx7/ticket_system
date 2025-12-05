package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.TicketOrder;
import com.ticket.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ticket.dto.CreateOrderRequest;
import com.ticket.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
@Validated
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // 创建订单

    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody @Valid CreateOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = RequestUtil.getUserId(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.createOrder(request, userId);
    }





    // 根据订单ID获取订单详情（用户端，只能查看自己的订单）
    @GetMapping("/{id}")
    public Result<TicketOrder> getOrderById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        TicketOrder order = orderService.getOrderById(id).getData();
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权查看此订单");
        }
        return Result.success(order);
    }

    // 取消订单
    @PostMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.cancelOrder(id, userId, request);
    }

    // 分页查询订单列表（支持条件查询：状态、演出ID）
    @GetMapping("/page")
    public Result<PageResult<TicketOrder>> getOrdersByPage(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long eventId,
            PageRequest pageRequest,
            HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        try {
            PageResult<TicketOrder> result = orderService.getOrdersByPageWithCondition(userId, status, eventId, pageRequest);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("分页查询订单失败: " + e.getMessage());
        }
    }

    // 更新订单
    @PutMapping("/{id}")
    public Result<String> updateOrder(@PathVariable Long id, @RequestBody TicketOrder order, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.updateOrder(id, order, userId);
    }

    // 删除订单
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return orderService.deleteOrder(id, userId);
    }

}