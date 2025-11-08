package com.example.event_management_system.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEditRequest {

    @Size(max = 30, message = "First name can't have more than 30 symbols")
    private String firstName;

    @Size(max = 30, message = "Last name can't have more than 30 symbols")
    private String lastName;

    @Email(message = "Requires correct email format")
    private String email;

    @org.hibernate.validator.constraints.URL(message = "Requires correct web link format")
    private String profilePicture;

    private String phoneNumber;

    private String currentPassword;

    @Pattern(regexp = "^$|.{3,20}$", message = "Password length must be between 3 and 20 characters!")
    private String newPassword;

    @Pattern(regexp = "^$|.{3,20}$", message = "Password length must be between 3 and 20 characters!")
    private String confirmNewPassword;
}

