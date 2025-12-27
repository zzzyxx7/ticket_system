package com.ticket.controller;

import com.ticket.annotation.AdminRequired;
import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.Event;
import com.ticket.service.EventService;
import com.ticket.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/event") // 管理端演出接口统一前缀
public class AdminEventController {

    @Autowired
    private EventService eventService;

    // 1. 添加演出信息
    @PostMapping
    @AdminRequired
    public Result<String> createEvent(@RequestBody Event event,
                                      HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request); // 作为创建人记录
        return eventService.createEvent(event, userId);
    }

    // 2. 修改演出信息
    @PutMapping("/{id}")
    @AdminRequired
    public Result<String> updateEvent(@PathVariable Long id,
                                      @RequestBody Event event,
                                      HttpServletRequest request) {
        Long userId = RequestUtil.getUserId(request); // 作为更新人记录
        return eventService.updateEvent(id, event, userId);
    }

    // 3. 删除演出信息
    @DeleteMapping("/{id}")
    @AdminRequired
    public Result<String> deleteEvent(@PathVariable Long id) {
        return eventService.deleteEvent(id);
    }

    // 4. 分页查询演出列表（管理端，可带条件，返回完整库存信息）
    @GetMapping("/page")
    @AdminRequired
    public Result<PageResult<EventDTO>> getEventsByPageForAdmin(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            PageRequest pageRequest) {

        // 使用管理端专用方法，返回完整库存信息
        PageResult<EventDTO> page =
                eventService.getEventsByConditionAndPageForAdmin(city, category, pageRequest);
        return Result.success(page);
    }

    // 5. 查询演出信息详情（管理端版：返回完整库存数字）
    @GetMapping("/{id}")
    @AdminRequired
    public Result<EventDTO> getEventDetailForAdmin(@PathVariable Long id) {
        // 使用管理端专用方法，返回完整库存信息
        return eventService.getEventByIdForAdmin(id);
    }
}