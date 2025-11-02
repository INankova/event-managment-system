package com.example.event_management_system.web.dto;

import com.example.event_management_system.Event.model.EventType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title must not be empty!")
    private String title;

    @NotBlank(message = "Date and time must not be empty!")
    private LocalDateTime dateTime;

    @NotBlank(message = "Price must not be empty!")
    private BigDecimal price;

    @NotBlank(message = "Location must not be empty!")
    private String location;

    @NotBlank(message = "Description must not be empty!")
    private String description;

    @NotBlank(message = "Image URL must not be empty!")
    @URL
    private String imageUrl;

    @NotBlank(message = "Event type must not be empty!")
    private EventType eventType;

    public CreateEventRequest() {}

}
