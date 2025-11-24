package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.Event;
import com.ticket.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    // 获取所有演出列表
    @GetMapping("/list")
    public Result<List<Event>> getAllEvents() {
        return eventService.getAllEvents();
    }

    // 根据ID获取演出详情
    @GetMapping("/{id}")
    public Result<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // 按城市查询演出
    @GetMapping("/city/{city}")
    public Result<List<Event>> getEventsByCity(@PathVariable String city) {
        return eventService.getEventsByCity(city);
    }

    // 按分类查询演出
    @GetMapping("/category/{category}")
    public Result<List<Event>> getEventsByCategory(@PathVariable String category) {
        return eventService.getEventsByCategory(category);
    }

    // 按城市和分类查询
    @GetMapping("/search")
    public Result<List<Event>> searchEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category) {

        return eventService.searchEvents(city, category);
    }

    // 搜索演出（按名称）
    @GetMapping("/searchByName")
    public Result<List<Event>> searchEventsByName(@RequestParam String keyword) {
        return eventService.searchEventsByName(keyword);
    }

    // 创建演出
    @PostMapping
    public Result<String> createEvent(@RequestBody Event event, HttpServletRequest request) {
        Long userId = getUserId(request);
        return eventService.createEvent(event, userId);
    }

    // 更新演出
    @PutMapping("/{id}")
    public Result<String> updateEvent(@PathVariable Long id, @RequestBody Event event, HttpServletRequest request) {
        Long userId = getUserId(request);
        return eventService.updateEvent(id, event, userId);
    }

    // 删除演出
    @DeleteMapping("/{id}")
    public Result<String> deleteEvent(@PathVariable Long id) {
        return eventService.deleteEvent(id);
    }

    private Long getUserId(HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        return userIdStr == null ? null : Long.valueOf(userIdStr);
    }
}