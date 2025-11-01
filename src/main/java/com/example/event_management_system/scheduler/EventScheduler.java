package com.example.event_management_system.scheduler;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.service.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventScheduler {
    private final EventService eventService;

    public EventScheduler(EventService eventService) {
        this.eventService = eventService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteExpiredEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> expiredEvents = eventService.findEventsBefore(now);

        for (Event event : expiredEvents) {
            eventService.deleteEvent(event.getId());
        }
    }
}
