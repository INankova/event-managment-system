package com.example.event_management_system.email.Client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EventReminderRequest {
    private UUID userId;
    private String subject;
    private String body;
    private LocalDateTime eventStart;
    private List<Integer> offsetsMinutes;
}
