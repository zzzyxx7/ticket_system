package com.ticket.listener;

import com.ticket.entity.TicketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息监听器（消费者）
 * 
 * 作用：监听RabbitMQ队列中的消息，当有消息到达时自动处理
 * 
 * 工作原理：
 * 1. Spring Boot启动后，会自动连接到RabbitMQ
 * 2. 监听配置的队列（如 "order.created"）
 * 3. 当队列中有新消息时，自动调用对应的方法
 * 4. 方法在独立的线程中执行，不影响主流程
 */
@Component
public class OrderMessageListener {
    
    private static final Logger log = LoggerFactory.getLogger(OrderMessageListener.class);
    
    /**
     * 监听订单创建消息
     * 
     * 当订单创建成功后，OrderServiceImpl 会发送消息到 "order.created" 队列
     * 这个方法会自动被调用，处理订单创建后的后续操作
     * 
     * @param order 订单信息（自动从消息中反序列化）
     */
    @RabbitListener(queues = "order.created")
    public void handleOrderCreated(TicketOrder order) {
        log.info("========== 收到订单创建消息，开始异步处理 ==========");
        log.info("订单ID: {}, 用户ID: {}, 事件ID: {}, 数量: {}, 总价: {}", 
            order.getId(), order.getUserId(), order.getEventId(), 
            order.getQuantity(), order.getTotalPrice());
        
        try {
            // ========== 这里是异步处理的核心 ==========
            // 以下操作都是耗时操作，但现在不会阻塞用户请求
            
            // 1. 发送订单确认通知（模拟耗时操作，实际可能是发送邮件/短信）
            sendOrderNotification(order);
            
            // 2. 记录详细操作日志（模拟写入文件或日志系统）
            logOrderDetail(order);
            
            // 3. 更新统计数据（模拟更新数据库统计表）
            updateOrderStatistics(order);
            
            // 4. 可以调用第三方系统（模拟调用外部API）
            notifyThirdPartySystem(order);
            
            log.info("========== 订单创建消息处理完成 ==========");
            
        } catch (Exception e) {
            log.error("处理订单创建消息失败, orderId={}", order.getId(), e);
            // 可以根据业务需求决定是否重试或记录失败
            // 这里只是记录日志，实际可以配置死信队列处理失败消息
        }
    }
    
    /**
     * 监听订单取消消息
     * 
     * @param order 订单信息
     */
    @RabbitListener(queues = "order.cancelled")
    public void handleOrderCancelled(TicketOrder order) {
        log.info("========== 收到订单取消消息，开始异步处理 ==========");
        log.info("订单ID: {}, 用户ID: {}, 事件ID: {}", 
            order.getId(), order.getUserId(), order.getEventId());
        
        try {
            // 处理订单取消后的业务逻辑
            sendCancellationNotification(order);
            logCancellationDetail(order);
            updateCancellationStatistics(order);
            
            log.info("========== 订单取消消息处理完成 ==========");
            
        } catch (Exception e) {
            log.error("处理订单取消消息失败, orderId={}", order.getId(), e);
        }
    }
    
    /**
     * 发送订单通知（模拟发送邮件/短信）
     */
    private void sendOrderNotification(TicketOrder order) {
        // 模拟耗时操作（实际可能是HTTP请求发送邮件或短信）
        try {
            Thread.sleep(100); // 模拟网络请求耗时
            log.info("✓ 已发送订单确认通知 - 订单ID: {}", order.getId());
            // 实际代码示例：
            // emailService.sendOrderConfirmation(order.getUserId(), order);
            // smsService.sendOrderConfirmation(order.getPhone(), order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 记录订单详细日志（模拟写入文件或日志系统）
     */
    private void logOrderDetail(TicketOrder order) {
        // 模拟耗时操作（实际可能是IO操作）
        try {
            Thread.sleep(50); // 模拟文件写入耗时
            log.info("✓ 已记录订单详细日志 - 订单ID: {}", order.getId());
            // 实际代码示例：
            // fileLogService.writeOrderLog(order);
            // logSystem.recordOrderOperation(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 更新订单统计数据（模拟更新数据库）
     */
    private void updateOrderStatistics(TicketOrder order) {
        // 模拟耗时操作（实际可能是数据库更新）
        try {
            Thread.sleep(50); // 模拟数据库操作耗时
            log.info("✓ 已更新订单统计数据 - 订单ID: {}", order.getId());
            // 实际代码示例：
            // statisticsService.incrementOrderCount(order.getEventId());
            // statisticsService.updateSalesAmount(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 通知第三方系统（模拟调用外部API）
     */
    private void notifyThirdPartySystem(TicketOrder order) {
        // 模拟耗时操作（实际可能是HTTP请求）
        try {
            Thread.sleep(200); // 模拟网络请求耗时
            log.info("✓ 已通知第三方系统 - 订单ID: {}", order.getId());
            // 实际代码示例：
            // httpClient.post("https://third-party-api.com/order", order);
            // analyticsService.trackOrderEvent(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 发送订单取消通知
     */
    private void sendCancellationNotification(TicketOrder order) {
        try {
            Thread.sleep(100);
            log.info("✓ 已发送订单取消通知 - 订单ID: {}", order.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 记录订单取消详细日志
     */
    private void logCancellationDetail(TicketOrder order) {
        try {
            Thread.sleep(50);
            log.info("✓ 已记录订单取消日志 - 订单ID: {}", order.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 更新订单取消统计数据
     */
    private void updateCancellationStatistics(TicketOrder order) {
        try {
            Thread.sleep(50);
            log.info("✓ 已更新取消统计数据 - 订单ID: {}", order.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}






