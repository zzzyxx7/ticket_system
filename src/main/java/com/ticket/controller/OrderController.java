package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.TicketOrder;
import com.ticket.mapper.TicketOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ticket.dto.CreateOrderRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    // 创建订单
    @PostMapping("/create/v2")
    public Result<String> createOrderV2(@RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {
        try {
            String userIdStr = (String) httpRequest.getAttribute("userId");
            if (userIdStr == null) {
                return Result.error("用户未登录");
            }

            TicketOrder order = new TicketOrder();
            order.setUserId(Long.valueOf(userIdStr));
            order.setEventId(request.getEventId());
            order.setQuantity(request.getQuantity());
            order.setTotalPrice(request.getTotalPrice());
            order.setStatus("PENDING");

            ticketOrderMapper.insert(order);
            return Result.success("订单创建成功，订单ID: " + order.getId());
        } catch (Exception e) {
            return Result.error("订单创建失败: " + e.getMessage());
        }
    }

    // 获取当前用户的所有订单
    @GetMapping("/list")
    public Result<List<TicketOrder>> getOrderList(HttpServletRequest request) {
        // 从请求属性中获取用户ID（后续会通过拦截器设置）
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            return Result.error("用户未登录");
        }

        Long userId = Long.valueOf(userIdStr);
        List<TicketOrder> orders = ticketOrderMapper.selectByUserId(userId);
        return Result.success(orders);
    }

    // 根据订单ID获取订单详情
    @GetMapping("/{id}")
    public Result<TicketOrder> getOrderById(@PathVariable Long id) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    // 取消订单
    @PostMapping("/cancel/{id}")
    public Result<String> cancelOrder(@PathVariable Long id) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }

        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("只能取消待支付的订单");
        }

        ticketOrderMapper.updateStatus(id, "CANCELLED");
        return Result.success("订单取消成功");
    }

    // 分页查询订单列表
    @GetMapping("/page")
    public Result<List<TicketOrder>> getOrdersByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) {
            return Result.error("用户未登录");
        }

        Long userId = Long.valueOf(userIdStr);
        int offset = (page - 1) * size;
        List<TicketOrder> orders = ticketOrderMapper.selectByUserIdAndPage(userId, offset, size);
        return Result.success(orders);
    }

    // 更新订单
    @PutMapping("/{id}")
    public Result<String> updateOrder(@PathVariable Long id, @RequestBody TicketOrder order, HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                return Result.error("用户未登录");
            }

            TicketOrder existingOrder = ticketOrderMapper.selectById(id);
            if (existingOrder == null) {
                return Result.error("订单不存在");
            }

            Long userId = Long.valueOf(userIdStr);
            // 验证订单属于当前用户
            if (!existingOrder.getUserId().equals(userId)) {
                return Result.error("无权修改此订单");
            }

            order.setId(id);
            order.setUserId(userId);
            ticketOrderMapper.update(order);
            return Result.success("订单更新成功");
        } catch (Exception e) {
            return Result.error("订单更新失败: " + e.getMessage());
        }
    }

    // 删除订单
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                return Result.error("用户未登录");
            }

            TicketOrder order = ticketOrderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            Long userId = Long.valueOf(userIdStr);
            // 验证订单属于当前用户
            if (!order.getUserId().equals(userId)) {
                return Result.error("无权删除此订单");
            }

            // 只有已取消的订单才能删除
            if (!"CANCELLED".equals(order.getStatus())) {
                return Result.error("只能删除已取消的订单");
            }

            ticketOrderMapper.deleteById(id);
            return Result.success("订单删除成功");
        } catch (Exception e) {
            return Result.error("订单删除失败: " + e.getMessage());
        }
    }

}