package com.example.event_management_system.email.Client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class NotificationRequest {

    private UUID userId;

    private String subject;

    private String body;
}
