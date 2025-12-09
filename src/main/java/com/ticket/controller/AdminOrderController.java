package com.ticket.controller;

import com.ticket.annotation.AdminRequired;
import com.ticket.common.Result;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.TicketOrder;
import com.ticket.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/order") // 管理端订单接口统一前缀
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 1. 管理端分页查询订单列表
     * 可根据业务需要设计查询条件：
     *   - userId：按用户筛选
     *   - status：订单状态
     *   - eventId：演出ID
     */
    @GetMapping("/page")
    @AdminRequired
    public Result<PageResult<TicketOrder>> getOrdersByPageForAdmin(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long eventId,
            PageRequest pageRequest) {

        PageResult<TicketOrder> page =
                orderService.getOrdersByPageForAdmin(userId, status, eventId, pageRequest);
        return Result.success(page);
    }

    /**
     * 2. 管理端查询订单详情
     * 可以查看任意订单，不限制当前用户
     */
    @GetMapping("/{id}")
    @AdminRequired
    public Result<TicketOrder> getOrderDetailForAdmin(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    /**
     * 3. 管理端修改订单信息（手动更新订单状态等）
     * 典型场景：人工干预状态、补单、关闭异常订单
     */
    @PutMapping("/{id}")
    @AdminRequired
    public Result<String> updateOrderForAdmin(@PathVariable Long id,
                                              @RequestBody TicketOrder order) {
        return orderService.updateOrderByAdmin(id, order);
    }

    /**
     * 4. 管理端删除订单
     * 典型策略：仅删除已取消订单，避免误删有效订单
     */
    @DeleteMapping("/{id}")
    @AdminRequired
    public Result<String> deleteOrderForAdmin(@PathVariable Long id) {
        return orderService.deleteOrderByAdmin(id);
    }
}