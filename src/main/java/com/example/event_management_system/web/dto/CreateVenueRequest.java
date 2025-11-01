package com.example.event_management_system.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateVenueRequest {

    @NotBlank(message = "Name must not be empty")
    private String name;

    @NotBlank(message = "Address must not be empty")
    private String address;

    @NotBlank(message = "Capacity must not be empty")
    private Integer capacity;

    @NotBlank(message = "City must not be empty")
    private String city;

    @NotBlank(message = "Contact info must not be empty")
    private String contactInfo;
}
