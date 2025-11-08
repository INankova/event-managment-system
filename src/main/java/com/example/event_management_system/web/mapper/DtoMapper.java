package com.example.event_management_system.web.mapper;

import com.example.event_management_system.User.model.User;
import com.example.event_management_system.web.dto.UserEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    // Test:
    public static UserEditRequest mapUserToUserEditRequest(User user) {

        return UserEditRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePictureUrl())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}