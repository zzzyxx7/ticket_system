package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.CreateOrderRequest;
import com.ticket.entity.TicketOrder;
import com.ticket.mapper.TicketOrderMapper;
import com.ticket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;


    @Override
    public Result<String> createOrder(CreateOrderRequest request, Long userId) {
        try {
            TicketOrder order = new TicketOrder();
            order.setUserId(userId);
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

    @Override
    public Result<List<TicketOrder>> getOrderList(Long userId) {
        try {
            List<TicketOrder> orders = ticketOrderMapper.selectByUserId(userId);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }

    @Override
    public Result<TicketOrder> getOrderById(Long id) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<String> cancelOrder(Long id, Long userId) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权取消此订单");
        }
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("只能取消待支付的订单");
        }
        ticketOrderMapper.updateStatus(id, "CANCELLED");
        return Result.success("订单取消成功");
    }

    @Override
    public Result<List<TicketOrder>> getOrdersByPage(Long userId, Integer page, Integer size) {
        try {
            int offset = (page - 1) * size;
            List<TicketOrder> orders = ticketOrderMapper.selectByUserIdAndPage(userId, offset, size);
            return Result.success(orders);
        } catch (Exception e) {
            return Result.error("分页查询订单失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> updateOrder(Long id, TicketOrder order, Long userId) {
        TicketOrder existingOrder = ticketOrderMapper.selectById(id);
        if (existingOrder == null) {
            return Result.error("订单不存在");
        }
        if (!existingOrder.getUserId().equals(userId)) {
            return Result.error("无权修改此订单");
        }
        order.setId(id);
        order.setUserId(userId);
        ticketOrderMapper.update(order);
        return Result.success("订单更新成功");
    }

    @Override
    @Transactional
    public Result<String> deleteOrder(Long id, Long userId) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权删除此订单");
        }
        if (!"CANCELLED".equals(order.getStatus())) {
            return Result.error("只能删除已取消的订单");
        }
        ticketOrderMapper.deleteById(id);
        return Result.success("订单删除成功");
    }
}


