package com.ticket;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 主应用类
 * 
 * @EnableRabbit: 启用RabbitMQ消息监听功能
 * 这个注解让 @RabbitListener 注解能够正常工作
 */
@SpringBootApplication
@EnableRabbit
public class TicketSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketSystemApplication.class, args);
    }

}
