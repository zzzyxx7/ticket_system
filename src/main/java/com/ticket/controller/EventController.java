package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.entity.Event;
import com.ticket.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventMapper eventMapper;

    // 获取所有演出列表
    @GetMapping("/list")
    public Result<List<Event>> getAllEvents() {
        List<Event> events = eventMapper.selectAll();
        return Result.success(events);
    }

    // 根据ID获取演出详情
    @GetMapping("/{id}")
    public Result<Event> getEventById(@PathVariable Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            return Result.error("演出不存在");
        }
        return Result.success(event);
    }

    // 按城市查询演出
    @GetMapping("/city/{city}")
    public Result<List<Event>> getEventsByCity(@PathVariable String city) {
        List<Event> events = eventMapper.selectByCity(city);
        return Result.success(events);
    }

    // 按分类查询演出
    @GetMapping("/category/{category}")
    public Result<List<Event>> getEventsByCategory(@PathVariable String category) {
        List<Event> events = eventMapper.selectByCategory(category);
        return Result.success(events);
    }

    // 按城市和分类查询
    @GetMapping("/search")
    public Result<List<Event>> searchEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category) {

        List<Event> events;
        if (city != null && category != null) {
            events = eventMapper.selectByCityAndCategory(city, category);
        } else if (city != null) {
            events = eventMapper.selectByCity(city);
        } else if (category != null) {
            events = eventMapper.selectByCategory(category);
        } else {
            events = eventMapper.selectAll();
        }

        return Result.success(events);
    }

    // 搜索演出（按名称）
    @GetMapping("/searchByName")
    public Result<List<Event>> searchEventsByName(@RequestParam String keyword) {
        List<Event> events = eventMapper.searchByName(keyword);
        return Result.success(events);
    }

    // 创建演出
    @PostMapping
    public Result<String> createEvent(@RequestBody Event event, HttpServletRequest request) {
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                event.setCreatedBy(Long.valueOf(userIdStr));
            }
            eventMapper.insert(event);
            return Result.success("演出创建成功，演出ID: " + event.getId());
        } catch (Exception e) {
            return Result.error("演出创建失败: " + e.getMessage());
        }
    }

    // 更新演出
    @PutMapping("/{id}")
    public Result<String> updateEvent(@PathVariable Long id, @RequestBody Event event, HttpServletRequest request) {
        try {
            Event existingEvent = eventMapper.selectById(id);
            if (existingEvent == null) {
                return Result.error("演出不存在");
            }

            event.setId(id);
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                event.setUpdatedBy(Long.valueOf(userIdStr));
            }

            eventMapper.update(event);
            return Result.success("演出更新成功");
        } catch (Exception e) {
            return Result.error("演出更新失败: " + e.getMessage());
        }
    }

    // 删除演出
    @DeleteMapping("/{id}")
    public Result<String> deleteEvent(@PathVariable Long id) {
        try {
            Event event = eventMapper.selectById(id);
            if (event == null) {
                return Result.error("演出不存在");
            }

            eventMapper.deleteById(id);
            return Result.success("演出删除成功");
        } catch (Exception e) {
            return Result.error("演出删除失败: " + e.getMessage());
        }
    }
}