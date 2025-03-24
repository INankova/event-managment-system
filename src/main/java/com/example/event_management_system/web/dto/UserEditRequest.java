package com.example.event_management_system.web.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
@Builder
public class UserEditRequest {

    @Size(max = 30, message = "First name can't have more than 30 symbols")
    private String firstName;

    @Size(max = 30, message = "Last name can't have more than 30 symbols")
    private String lastName;

    @URL(message = "Requires correct web link format")
    private String profilePicture;
}
