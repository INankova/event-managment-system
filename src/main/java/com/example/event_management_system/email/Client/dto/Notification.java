package com.example.event_management_system.email.Client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    private String subject;

    private String status;

    private LocalDateTime createdOn;

    private String type;
}
