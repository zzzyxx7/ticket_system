package com.ticket.controller;

import com.ticket.common.Result;
import com.ticket.dto.EventDTO;
import com.ticket.dto.PageRequest;
import com.ticket.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ticket.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {

    @Autowired
    private EventService eventService;

    // 首页演出列表：根据地区与分类返回推荐列表
    // 默认城市为“北京”，暂时按城市维度返回，分类由前端自行分组展示
    @GetMapping("/home")
    public Result<List<EventDTO>> getHomeEvents(
            @RequestParam(required = false) String city,
            HttpServletRequest request) {
        // 如果没有传城市参数，默认北京
        if (city == null || city.isEmpty()) {
            city = "北京";
            // TODO: 已登录时可以根据 IP 解析城市，替换掉默认值
            // String ip = request.getRemoteAddr();
            // city = ipToCity(ip);
        }
        // 这里复用按城市+分类查询的业务逻辑，暂时不强制四大类，由前端按分类字段分组展示
        return eventService.searchEvents(city, null);
    }

    // 获取所有演出列表
    @GetMapping("/list")
    public Result<List<EventDTO>> getAllEvents() {
        return eventService.getAllEvents();
    }

    // 根据ID获取演出详情
    @GetMapping("/{id}")
    public Result<EventDTO> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // 按城市查询演出
    @GetMapping("/city/{city}")
    public Result<List<EventDTO>> getEventsByCity(@PathVariable String city) {
        return eventService.getEventsByCity(city);
    }

    // 按分类查询演出
    @GetMapping("/category/{category}")
    public Result<List<EventDTO>> getEventsByCategory(@PathVariable String category) {
        return eventService.getEventsByCategory(category);
    }

    // 按城市和分类查询
    @GetMapping("/search")
    public Result<List<EventDTO>> searchEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category) {


        return eventService.searchEvents(city, category);
    }

    // 搜索演出（按名称）
    @GetMapping("/searchByName")
    public Result<List<EventDTO>> searchEventsByName(@RequestParam String keyword) {
        return eventService.searchEventsByName(keyword);
    }

    // 演出分页列表
    @GetMapping("/page")
    public Result<PageResult<EventDTO>> getEventsByPage(PageRequest pageRequest) {
        try {
            PageResult<EventDTO> result = eventService.getEventsByPage(pageRequest);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("分页查询失败: " + e.getMessage());
        }
    }

    // 条件分页查询
        @GetMapping("/page/search")
        public Result<PageResult<EventDTO>> searchEventsByPage(
                @RequestParam(required = false) String city,
                @RequestParam(required = false) String category,
                PageRequest pageRequest) {
            try {
                // 调用带条件的分页方法
                PageResult<EventDTO> result = eventService.getEventsByConditionAndPage(city, category, pageRequest);
                return Result.success(result);
            } catch (Exception e) {
                return Result.error("条件分页搜索失败: " + e.getMessage());
            }
        }
            }