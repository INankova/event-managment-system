package com.example.event_management_system.Event.service;


import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Cacheable(value = "event", key = "#eventId")
    public Event getEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new DomainException("This event was not found: " + id));
    }

    @CacheEvict(value = "event", key = "#eventId")
    public void deleteEvent(@PathVariable UUID id) {
        eventRepository.deleteById(id);
    }

    @CacheEvict(value = "events", key = "#eventId")
    public void editEventDetails(UUID EventId, @Valid EventEditRequest eventEditRequest) {

        Event event = getEventById(EventId);

        event.setTitle(eventEditRequest.getTitle());
        event.setDateTime(eventEditRequest.getDateTime());
        event.setEventType(eventEditRequest.getEventType());
        event.setLocation(eventEditRequest.getLocation());
        event.setPrice(eventEditRequest.getPrice());
        event.setDescription(eventEditRequest.getDescription());
        event.setEventImageUrl(eventEditRequest.getImageUrl());

        eventRepository.save(event);
    }

    @CacheEvict(value = "events", allEntries = true)
    public void createNewEvent(CreateEventRequest createEventRequest, User user) {
        Event event = Event.builder()
                .title(createEventRequest.getTitle())
                .description(createEventRequest.getDescription())
                .location(createEventRequest.getLocation())
                .price(createEventRequest.getPrice())
                .dateTime(createEventRequest.getDateTime())
                .eventImageUrl(createEventRequest.getImageUrl())
                .eventType(createEventRequest.getEventType())
                .build();

        eventRepository.save(event);
    }

    @Cacheable("events")
    public List<Event> findEventsBefore(LocalDateTime time) {
        return eventRepository.findByDateTimeBefore(time);
    }
}
