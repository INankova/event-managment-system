package com.example.event_management_system.email.Client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    private String subject;

    private String status;

    private String body;

    private LocalDateTime createdOn;

    private String type;
}
