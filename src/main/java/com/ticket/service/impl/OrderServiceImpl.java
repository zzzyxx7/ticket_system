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
import com.ticket.util.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private TicketOrderMapper ticketOrderMapper;
    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private DistributedLock distributedLock;


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

        // 2. 使用分布式锁保证并发安全
        // 锁的粒度：按事件ID加锁，同一事件的订单创建需要串行化
        String lockKey = "lock:order:create:" + eventId;
        
        // 尝试获取锁（等待最多500ms，锁持有时间10秒）
        boolean lockAcquired = distributedLock.tryLockWithWait(
            lockKey, 500, 10, TimeUnit.SECONDS
        );
        
        if (!lockAcquired) {
            log.warn("获取分布式锁失败，系统繁忙, userId={}, eventId={}", userId, eventId);
            return Result.error("系统繁忙，请稍后重试");
        }
        
        try {
            // 3. 查询演出信息（在锁内查询，避免库存被其他线程修改）
            Event event = eventMapper.selectById(eventId);
            if (event == null) {
                throw new BusinessException("演出不存在");
            }
            if (event.getPrice() == null) {
                throw new BusinessException("演出价格未设置");
            }
            
            // 4. 检查库存是否充足（双重检查）
            if (event.getStock() == null || event.getStock() < quantity) {
                return Result.error("库存不足，抢票失败");
            }

            // 5. 尝试扣减库存（使用数据库乐观锁 + 分布式锁双重保障）
            // 对应 SQL: UPDATE event SET stock = stock - ? WHERE id = ? AND stock >= ?
            int rows = eventMapper.decreaseStock(eventId, quantity);
            if (rows == 0) {
                // 扣减失败，说明库存不足或其他人已经抢完（虽然已经加锁，但双重检查更安全）
                return Result.error("库存不足，抢票失败");
            }

            // 6. 计算总价
            BigDecimal totalPrice = event.getPrice().multiply(new BigDecimal(quantity));

            // 7. 创建订单
            TicketOrder order = new TicketOrder();
            order.setUserId(userId);
            order.setEventId(eventId);
            order.setQuantity(quantity);
            order.setTotalPrice(totalPrice);
            order.setStatus("PENDING");
            order.setCreatedBy(userId);

            int insertRows = ticketOrderMapper.insert(order);
            if (insertRows <= 0) {
                // 插入订单失败，抛异常触发事务回滚（库存也会回滚）
                throw new BusinessException("创建订单失败");
            }

            log.info("订单创建成功, orderId={}, userId={}, eventId={}, quantity={}", 
                order.getId(), userId, eventId, quantity);
            
            // 8. 返回结果
            return Result.success("抢票成功，订单ID：" + order.getId());
            
        } finally {
            // 释放分布式锁（确保在 finally 中释放，避免死锁）
            distributedLock.unlock(lockKey);
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
    public Result<String> cancelOrder(Long id, Long userId) {
        // 1. 查询订单
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        
        // 2. 权限校验
        if (!order.getUserId().equals(userId)) {
            return Result.error("无权取消此订单");
        }
        
        // 3. 状态校验
        if (!"PENDING".equals(order.getStatus())) {
            return Result.error("只能取消待支付的订单");
        }
        
        // 4. 使用分布式锁保证库存回滚的并发安全
        // 虽然取消订单并发不高，但为了数据一致性，还是加锁更安全
        String lockKey = "lock:order:cancel:" + order.getEventId();
        boolean lockAcquired = distributedLock.tryLockWithWait(
            lockKey, 500, 5, TimeUnit.SECONDS
        );
        
        if (!lockAcquired) {
            log.warn("获取分布式锁失败，取消订单失败, orderId={}, userId={}", id, userId);
            return Result.error("系统繁忙，请稍后重试");
        }
        
        try {
            // 5. 回滚库存（使用乐观锁保证并发安全）
            int stockRows = eventMapper.increaseStock(order.getEventId(), order.getQuantity());
            if (stockRows == 0) {
                // 回滚失败，可能演出不存在（理论上不应该发生）
                return Result.error("回滚库存失败，演出不存在");
            }

            // 6. 更新订单状态
            ticketOrderMapper.updateStatus(id, "CANCELLED", userId);
            
            log.info("订单取消成功, orderId={}, userId={}, eventId={}", 
                id, userId, order.getEventId());
            
            return Result.success("订单取消成功");
            
        } finally {
            // 释放分布式锁
            distributedLock.unlock(lockKey);
        }
    }

    @Override
    public PageResult<TicketOrder> getOrdersByPageWithCondition(Long userId, String status, Long eventId, PageRequest pageRequest) {
        // 处理分页参数
        if (pageRequest.getPage() == null || pageRequest.getPage() < 1) {
            pageRequest.setPage(1);
        }
        if (pageRequest.getSize() == null || pageRequest.getSize() < 1) {
            pageRequest.setSize(10);
        }
        int offset = (pageRequest.getPage() - 1) * pageRequest.getSize();
        int size = pageRequest.getSize();

        // 查询总数 + 当前页数据
        // TODO：Mybatis分页插件同理
        Long total = ticketOrderMapper.countByUserCondition(userId, status, eventId);
        List<TicketOrder> list = ticketOrderMapper.selectByUserCondition(
                userId, status, eventId, offset, size
        );

        return new PageResult<>(list, total, pageRequest);
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
    @Transactional
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

    @Override
    @Transactional
    public Result<String> deleteOrderByAdmin(Long id) {
        TicketOrder order = ticketOrderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"CANCELLED".equals(order.getStatus())) {
            return Result.error("只能删除已取消的订单");
        }
        ticketOrderMapper.deleteById(id);
        return Result.success("订单删除成功");
    }
}


