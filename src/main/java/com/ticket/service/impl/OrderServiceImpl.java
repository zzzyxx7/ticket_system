package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.CreateOrderRequest;
import com.ticket.entity.Event;
import com.ticket.entity.TicketOrder;
import com.ticket.exception.BusinessException;
import com.ticket.mapper.EventMapper;
import com.ticket.mapper.TicketOrderMapper;
import com.ticket.service.OrderService;
import com.ticket.util.AuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;
    @Autowired
    private EventMapper eventMapper;


    @Override
    public Result<String> createOrder(CreateOrderRequest request, Long userId) {
        try {
            TicketOrder order = new TicketOrder();
            // 检查演出是否存在
            Long eventId = order.getEventId();
            Event event = eventMapper.selectById(eventId);
            if (event == null) {
                throw new BusinessException("演出不存在");
            }

            // 检查库存
            if (event.getStock() < order.getQuantity()) {
                throw new BusinessException("库存不足");
            }
            order.setUserId(userId);
            order.setEventId(request.getEventId());
            order.setQuantity(request.getQuantity());
            order.setTotalPrice(request.getTotalPrice());
            order.setStatus("PENDING");
            // 调用审计工具类设置创建时间、更新人等字段
            AuditUtil.setCreateAuditFields(order, userId);
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
    @Transactional
    public Result<String> cancelOrder(Long id, Long userId, HttpServletRequest request) { // 新增 request 参数
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
        Event event = eventMapper.selectById(order.getEventId());
        event.setStock(event.getStock() + order.getQuantity());
        eventMapper.update(event);
        // 使用传入的 request 调用审计工具，设置更新人、更新时间
        AuditUtil.setUpdateAuditFields(order, request);

        // 修正 mapper 调用参数（只传 id 和 status）
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


