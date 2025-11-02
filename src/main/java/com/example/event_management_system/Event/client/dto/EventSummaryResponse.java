package com.example.event_management_system.Event.client.dto;

import com.example.event_management_system.Event.model.EventType;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventSummaryResponse {

    private UUID id;

    private String title;

    private LocalDateTime dateTime;

    private String location;

    @DecimalMin("0.0")
    private BigDecimal price;

    private EventType eventType;
}
