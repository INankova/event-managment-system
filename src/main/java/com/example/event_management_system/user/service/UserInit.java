package com.example.event_management_system.user.service;

import com.example.event_management_system.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit  implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public UserInit(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!userService.getAllUsers().isEmpty()) {
            return;
        }

        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("Ivana")
                .email("ivana@gmail.com")
                .password("mrunki")
                .confirmPassword("mrunki")
                .build();

        userService.register(registerRequest);
    }
}
