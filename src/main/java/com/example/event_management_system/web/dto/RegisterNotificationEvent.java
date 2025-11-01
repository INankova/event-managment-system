package com.example.event_management_system.web.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RegisterNotificationEvent {

    private UUID userId;
    private String email;
}
