package com.example.event_management_system.Event.service;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TicketRepository ticketRepository;

    @Autowired
    public EventService(EventRepository eventRepository, ApplicationEventPublisher eventPublisher, TicketRepository ticketRepository) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
        this.ticketRepository = ticketRepository;
    }

    @Cacheable(value = "event", key = "#id")
    public Event getEventById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new DomainException("This event was not found: " + id));
    }

    @CacheEvict(value = "event", key = "#id")
    public void deleteEvent(UUID id) {
        eventRepository.deleteById(id);
    }

    @CacheEvict(value = "event", key = "#eventId")
    public void editEventDetails(UUID eventId, @Valid EventEditRequest eventEditRequest) {
        Event event = getEventById(eventId);

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
                .owner(user)
                .build();

        eventRepository.save(event);

        eventPublisher.publishEvent(
                new RegisterNotificationEvent(
                        user.getId(),
                        user.getEmail(),
                        event.getTitle(),
                        event.getDateTime()
                )
        );
    }

    @Cacheable("events")
    public List<Event> findEventsBefore(LocalDateTime time) {
        return eventRepository.findByDateTimeBefore(time);
    }

    @Transactional
    @CacheEvict(value = "event", key = "#id")
    public void deleteEventWithTickets(UUID id) {

        ticketRepository.deleteByEventId(id);
        
        eventRepository.deleteById(id);
    }
}
