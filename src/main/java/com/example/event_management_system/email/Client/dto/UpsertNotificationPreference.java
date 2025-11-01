package com.example.event_management_system.email.Client.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UpsertNotificationPreference {

    private UUID userId;

    private boolean enabled;

    private String type;

    private String contactInfo;
}
