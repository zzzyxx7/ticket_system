package com.ticket.controller;

import com.ticket.annotation.AdminRequired;
import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import com.ticket.entity.Event;
import com.ticket.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        Long userId = getUserId(request); // 作为创建人记录
        return eventService.createEvent(event, userId);
    }

    // 2. 修改演出信息
    @PutMapping("/{id}")
    @AdminRequired
    public Result<String> updateEvent(@PathVariable Long id,
                                      @RequestBody Event event,
                                      HttpServletRequest request) {
        Long userId = getUserId(request); // 作为更新人记录
        return eventService.updateEvent(id, event, userId);
    }

    // 3. 删除演出信息
    @DeleteMapping("/{id}")
    @AdminRequired
    public Result<String> deleteEvent(@PathVariable Long id) {
        return eventService.deleteEvent(id);
    }

    // 4. 分页查询演出列表（管理端，可带条件）
    @GetMapping("/page")
    @AdminRequired
    public Result<PageResult<EventDTO>> getEventsByPageForAdmin(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            PageRequest pageRequest) {

        // 如果你希望管理端和用户端分页逻辑一样，可以直接复用已有的:
        PageResult<EventDTO> page =
                eventService.getEventsByConditionAndPage(city, category, pageRequest);
        return Result.success(page);
    }

    // 5. 查询演出信息详情（管理端版：需要返回明确库存数字等）
    @GetMapping("/{id}")
    @AdminRequired
    public Result<EventDTO> getEventDetailForAdmin(@PathVariable Long id) {
        // 目前可以先复用用户端的 getEventById，
        // 后续在 EventDTO 里扩展库存等字段，并在 service 里加一个专门方法
        return eventService.getEventById(id);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        return userIdStr == null ? null : Long.valueOf(userIdStr);
    }
}