package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.entity.Event;

import java.util.List;

public interface EventService {

    Result<List<Event>> getAllEvents();

    Result<Event> getEventById(Long id);

    Result<List<Event>> getEventsByCity(String city);

    Result<List<Event>> getEventsByCategory(String category);

    Result<List<Event>> searchEvents(String city, String category);

    Result<List<Event>> searchEventsByName(String keyword);

    Result<String> createEvent(Event event, Long userId);

    Result<String> updateEvent(Long id, Event event, Long userId);

    Result<String> deleteEvent(Long id);
}


