package com.example.event_management_system.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterNotificationEvent {

    private UUID userId;
    private String email;
    private String eventName;
    private LocalDateTime eventStart;

}

