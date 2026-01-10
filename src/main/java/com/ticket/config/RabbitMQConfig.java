package com.ticket.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 * 定义消息队列
 */
@Configuration
public class RabbitMQConfig {
    
    /**
     * 订单创建队列
     * 当订单创建成功后，会发送消息到这个队列
     */
    @Bean
    public Queue orderCreatedQueue() {
        // 参数说明：
        // - "order.created": 队列名称
        // - true: 持久化队列（服务器重启后队列仍然存在）
        return new Queue("order.created", true);
    }
    
    /**
     * 订单取消队列
     * 当订单取消后，会发送消息到这个队列
     */
    @Bean
    public Queue orderCancelledQueue() {
        return new Queue("order.cancelled", true);
    }
}

