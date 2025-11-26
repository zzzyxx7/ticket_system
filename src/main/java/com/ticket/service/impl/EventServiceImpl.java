package com.ticket.service.impl;

import com.ticket.common.Result;
import com.ticket.entity.Event;
import com.ticket.mapper.EventMapper;
import com.ticket.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;

    @Override
    public Result<List<Event>> getAllEvents() {
        return Result.success(eventMapper.selectAll());
    }

    @Override
    public Result<Event> getEventById(Long id) {
        Event event = eventMapper.selectById(id);
        if (event == null) {
            return Result.error("演出不存在");
        }
        return Result.success(event);
    }

    @Override
    public Result<List<Event>> getEventsByCity(String city) {
        return Result.success(eventMapper.selectByCity(city));
    }

    @Override
    public Result<List<Event>> getEventsByCategory(String category) {
        return Result.success(eventMapper.selectByCategory(category));
    }

    @Override
    public Result<List<Event>> searchEvents(String city, String category) {
        if (city != null && category != null) {
            return Result.success(eventMapper.selectByCityAndCategory(city, category));
        } else if (city != null) {
            return Result.success(eventMapper.selectByCity(city));
        } else if (category != null) {
            return Result.success(eventMapper.selectByCategory(category));
        } else {
            return Result.success(eventMapper.selectAll());
        }
    }

    @Override
    public Result<List<Event>> searchEventsByName(String keyword) {
        return Result.success(eventMapper.searchByName(keyword));
    }

    @Override
    public Result<String> createEvent(Event event, Long userId) {
        try {
            if (userId != null) {
                event.setCreatedBy(userId);
            }
            eventMapper.insert(event);
            return Result.success("演出创建成功，演出ID: " + event.getId());
        } catch (Exception e) {
            return Result.error("演出创建失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> updateEvent(Long id, Event event, Long userId) {
        try {
            Event existingEvent = eventMapper.selectById(id);
            if (existingEvent == null) {
                return Result.error("演出不存在");
            }
            event.setId(id);
            if (userId != null) {
                event.setUpdatedBy(userId);
            }
            eventMapper.update(event);
            return Result.success("演出更新成功");
        } catch (Exception e) {
            return Result.error("演出更新失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> deleteEvent(Long id) {
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


