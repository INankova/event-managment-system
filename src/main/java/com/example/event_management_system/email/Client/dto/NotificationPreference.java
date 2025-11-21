package com.example.event_management_system.email.Client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPreference {

    private String type;

    private boolean enabled;

    private String contactInfo;
}
