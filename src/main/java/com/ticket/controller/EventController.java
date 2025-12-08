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
    // 默认城市为"北京"，返回四大类演出（演唱会、话剧、音乐会、体育赛事）
    @GetMapping("/home")
    public Result<List<EventDTO>> getHomeEvents(
            @RequestParam(required = false) String city,
            HttpServletRequest request) {
        // 如果没有传城市参数，默认北京
        if (city == null || city.isEmpty()) {
            city = "北京";
            // String ip = request.getRemoteAddr();
            // city = ipToCity(ip);
        }
        // 调用专门的首页推荐方法，只返回四大类演出
        return eventService.getHomeEvents(city);
    }

    // 搜索演出（匹配演出名/明星名）
    @GetMapping("/searchByName")
    public Result<PageResult<EventDTO>> searchEvents(
            @RequestParam String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            PageRequest pageRequest) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }
        PageResult<EventDTO> result = eventService.searchEventsByNameAndCondition(
                keyword.trim(), city, category, pageRequest);
        return Result.success(result);
    }

    // 根据ID获取演出详情
    @GetMapping("/{id}")
    public Result<EventDTO> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    // 条件分页查询演出（支持城市、分类、分页）
        @GetMapping("/page/search")
        public Result<PageResult<EventDTO>> searchEventsByPage(
                @RequestParam(required = false) String city,
                @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
                PageRequest pageRequest) {
            try {
            // 如果有关键词，先按名称搜索，再应用城市/分类筛选
            if (keyword != null && !keyword.isEmpty()) {
                // 调用带关键词的条件分页查询
                PageResult<EventDTO> result = eventService.searchEventsByNameAndCondition(keyword, city, category, pageRequest);
                return Result.success(result);
            } else {
                // 普通的条件分页查询
                PageResult<EventDTO> result = eventService.getEventsByConditionAndPage(city, category, pageRequest);
                return Result.success(result);
            }
            } catch (Exception e) {
                return Result.error("条件分页搜索失败: " + e.getMessage());
            }
        }
}