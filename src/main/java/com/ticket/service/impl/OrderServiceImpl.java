package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.dto.CreateOrderRequest;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
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
    @Transactional
    public Result<String> createOrder(CreateOrderRequest request, Long userId) {
        // 1. 基本参数校验
        if (request == null || request.getEventId() == null || request.getQuantity() == null) {
            return Result.error("参数不完整");
        }
        Long eventId = request.getEventId();
        Integer quantity = request.getQuantity();
        if (quantity <= 0) {
            return Result.error("购票数量必须大于0");
        }

        // 2. 尝试扣减库存（并发安全关键点）
        // 对应 SQL: UPDATE event SET stock = stock - ? WHERE id = ? AND stock >= ?
        int rows = eventMapper.decreaseStock(eventId, quantity);
        if (rows == 0) {
            // 扣减失败，说明库存不足或其他人已经抢完
            return Result.error("库存不足，抢票失败");
        }

        // 3. 查询演出价格，计算总价
        Event event = eventMapper.selectById(eventId);
        if (event == null) {
            // 理论上不应该出现：库存刚扣完，演出却查不到
            // 为了数据一致性，抛出异常回滚事务
            throw new BusinessException("演出不存在");
        }
        if (event.getPrice() == null) {
            throw new BusinessException("演出价格未设置");
        }

        BigDecimal totalPrice = event.getPrice().multiply(new BigDecimal(quantity));

        // 4. 创建订单
        TicketOrder order = new TicketOrder();
        order.setUserId(userId);
        order.setEventId(eventId);
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING"); // 或者根据业务设为 "PAID"
        AuditUtil.setCreateAuditFields(order, userId);

        int insertRows = ticketOrderMapper.insert(order);
        if (insertRows <= 0) {
            // 插入订单失败，抛异常触发事务回滚（库存也会回滚）
            throw new BusinessException("创建订单失败");
        }

        // 5. 返回结果（这里返回简单提示 + 订单ID）
        return Result.success("抢票成功，订单ID：" + order.getId());
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

    @Override
    public PageResult<TicketOrder> getOrdersByPageForAdmin(Long userId,
                                                           String status,
                                                           Long eventId,
                                                           PageRequest pageRequest) {
        // 1. 处理分页参数
        if (pageRequest.getPage() == null || pageRequest.getPage() < 1) {
            pageRequest.setPage(1);
        }
        if (pageRequest.getSize() == null || pageRequest.getSize() < 1) {
            pageRequest.setSize(10);
        }
        int offset = (pageRequest.getPage() - 1) * pageRequest.getSize();
        int size = pageRequest.getSize();

        // 2. 查询总数 + 当前页数据
        Long total = ticketOrderMapper.countByAdminCondition(userId, status, eventId);
        List<TicketOrder> list = ticketOrderMapper.selectByAdminCondition(
                userId, status, eventId, offset, size
        );

        return new PageResult<>(list, total, pageRequest);
    }

    @Override
    public Result<String> updateOrderByAdmin(Long id, TicketOrder order) {
        // 管理端更新，不校验 userId，只按订单ID更新允许字段（比如 status、remark 等）
        TicketOrder exist = ticketOrderMapper.selectById(id);
        if (exist == null) {
            return Result.error("订单不存在");
        }

        order.setId(id);
        int rows = ticketOrderMapper.updateByAdmin(order);
        if (rows <= 0) {
            return Result.error("更新订单失败");
        }
        return Result.success("更新订单成功");
    }
}


