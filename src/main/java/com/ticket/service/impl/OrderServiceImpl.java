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

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TicketOrderMapper ticketOrderMapper;
    @Autowired
    private EventMapper eventMapper;


    @Override
    @Transactional // 新增事务注解，保证库存扣减和订单创建的原子性
    public Result<String> createOrder(CreateOrderRequest request, Long userId) {
        try {
            // 1. 参数校验（兜底）
            if (request.getEventId() == null) {
                throw new BusinessException("演出ID不能为空");
            }
            if (request.getQuantity() == null || request.getQuantity() <= 0) {
                throw new BusinessException("购买数量必须为正整数");
            }

            // 2. 检查演出是否存在
            Long eventId = request.getEventId();
            Event event = eventMapper.selectById(eventId);
            if (event == null) {
                throw new BusinessException("演出不存在");
            }

            // 3. 检查库存
            int buyQuantity = request.getQuantity();
            if (event.getStock() < buyQuantity) {
                throw new BusinessException("库存不足");
            }

            // 4. 扣减库存（核心逻辑）
            int newStock = event.getStock() - buyQuantity;
            event.setStock(newStock); // 更新库存为扣减后的值
            eventMapper.update(event); // 将扣减后的库存保存到数据库

            // 5. 后端计算总金额
            BigDecimal totalPrice = event.getPrice().multiply(new BigDecimal(buyQuantity));

            // 6. 初始化订单并赋值
            TicketOrder order = new TicketOrder();
            order.setUserId(userId);
            order.setEventId(eventId);
            order.setQuantity(buyQuantity);
            order.setTotalPrice(totalPrice);
            order.setStatus("PENDING");

            // 7. 设置审计字段并保存订单
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
        ticketOrderMapper.updateStatus(id, "CANCELLED", order.getUserId());
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
    public Result<String> updateOrder(Long id, TicketOrder orderParam, Long userId) {
        // 1. 查询订单是否存在
        TicketOrder existingOrder = ticketOrderMapper.selectById(id);
        if (existingOrder == null) {
            return Result.error("订单不存在");
        }

        // 2. 校验权限（只能改自己的订单）
        if (!existingOrder.getUserId().equals(userId)) {
            return Result.error("无权修改此订单");
        }

        // 3. 只封装需要更新的字段
        TicketOrder updateOrder = new TicketOrder();
        updateOrder.setId(id); // 订单ID（从URL路径来）
        updateOrder.setStatus(orderParam.getStatus()); // 要改的状态
        updateOrder.setUpdatedBy(userId); // 操作人ID

        // 4. 执行更新
        ticketOrderMapper.update(updateOrder);
        return Result.success("订单状态更新成功");
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


