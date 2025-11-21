package com.example.event_management_system.email.Client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationScheduleRequest {
    private UUID userId;
    private String subject;
    private String body;
    private LocalDateTime scheduledAt;
}