package com.example.event_management_system.scheduler;

import com.example.event_management_system.event.model.Event;
import com.example.event_management_system.event.service.EventService;
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
        System.out.println("### deleteExpiredEvents RUNNING at " + now);

        List<Event> expiredEvents = eventService.findEventsBefore(now);

        for (Event event : expiredEvents) {
            System.out.println("### Deleting expired event: " + event.getId());
            eventService.deleteEventWithTickets(event.getId());
        }
    }
}
