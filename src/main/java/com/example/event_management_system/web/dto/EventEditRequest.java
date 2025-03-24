package com.example.event_management_system.web.dto;

import com.example.event_management_system.Event.model.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class EventEditRequest {

    @Size(max = 30, message = "Title can't have more than 20 symbols")
    private String title;

    private EventType eventType;

    private LocalDateTime dateTime;

    @DecimalMin("0.0")
    private BigDecimal price;

    private String location;

    @Size(max = 400, message = "Description can't have more than 500 symbols")
    private String description;

    @URL(message = "Image URL must not be empty!")
    private String imageUrl;
}
