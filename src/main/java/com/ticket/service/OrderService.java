package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.dto.CreateOrderRequest;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.TicketOrder;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface OrderService {

    Result<String> createOrder(CreateOrderRequest request, Long userId);

    Result<List<TicketOrder>> getOrderList(Long userId);

    Result<TicketOrder> getOrderById(Long id);

    Result<String> cancelOrder(Long id, Long userId, HttpServletRequest request);

    Result<List<TicketOrder>> getOrdersByPage(Long userId, Integer page, Integer size);

    Result<String> updateOrder(Long id, TicketOrder order, Long userId);

    Result<String> deleteOrder(Long id, Long userId);

    // ===== 管理端新增：分页查询订单（可按条件） =====
    PageResult<TicketOrder> getOrdersByPageForAdmin(Long userId,
                                                    String status,
                                                    Long eventId,
                                                    PageRequest pageRequest);

    // ===== 管理端新增：手动更新订单信息 =====
    Result<String> updateOrderByAdmin(Long id, TicketOrder order);


}


