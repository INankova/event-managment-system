package com.example.event_management_system.web.dto;

import com.example.event_management_system.Event.model.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEditRequest {

    private UUID id;

    @Size(max = 30, message = "Title can't have more than 20 symbols")
    private String title;

    private EventType eventType;

    @FutureOrPresent(message = "Date time should be future or now")
    private LocalDateTime dateTime;

    @DecimalMin("0.0")
    private BigDecimal price;

    private String location;

    @Size(max = 400, message = "Description can't have more than 500 symbols")
    private String description;

    @URL(message = "Image URL must not be empty!")
    private String imageUrl;

    public EventEditRequest(com.example.event_management_system.Event.model.Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.eventType = event.getEventType();
        this.dateTime = event.getDateTime();
        this.price = event.getPrice();
        this.location = event.getLocation();
        this.description = event.getDescription();
        this.imageUrl = event.getEventImageUrl();
    }
}
