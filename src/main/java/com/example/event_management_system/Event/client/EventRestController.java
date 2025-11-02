package com.example.event_management_system.Event.client;

import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Event.client.dto.EventSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventRestController {

    private final EventRepository eventRepository;

    @GetMapping
    public List<EventSummaryResponse> listBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return eventRepository.findAllByDateTimeBetween(from, to).stream()
                .map(e -> EventSummaryResponse.builder()
                        .id(e.getId())
                        .title(e.getTitle())
                        .dateTime(e.getDateTime())
                        .location(e.getLocation())
                        .price(e.getPrice())
                        .eventType(e.getEventType())
                        .build())
                .toList();
    }
}
