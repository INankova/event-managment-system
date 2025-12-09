package com.example.event_management_system.web.dto;

import com.example.event_management_system.event.model.EventType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title must not be empty!")
    private String title;

    @NotNull(message = "Date and time must not be empty!")
    @FutureOrPresent(message = "Date must be in the future or now!")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dateTime;

    @NotNull(message = "Price must not be empty!")
    @Positive(message = "Price must be positive!")
    private BigDecimal price;

    @NotBlank(message = "Location must not be empty!")
    private String location;

    @NotBlank(message = "Description must not be empty!")
    private String description;

    @NotBlank(message = "Image URL must not be empty!")
    @URL(message = "Image URL must be valid!")
    private String imageUrl;

    @NotNull(message = "Event type must not be empty!")
    private EventType eventType;
}
