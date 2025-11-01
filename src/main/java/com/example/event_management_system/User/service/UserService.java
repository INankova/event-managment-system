package com.example.event_management_system.User.service;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.web.dto.LoginRequest;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import com.example.event_management_system.web.dto.RegisterRequest;
import com.example.event_management_system.web.dto.UserEditRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserService(UserRepository userRepository, UserService userService, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(registerRequest.getUsername(), registerRequest.getEmail());
        if (optionalUser.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("User created: " + user.getUsername());

        RegisterNotificationEvent event = RegisterNotificationEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .build();

        eventPublisher.publishEvent(event);


        return user;

    }

    private User initializeUser(RegisterRequest registerRequest) {

        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .confirmPassword(registerRequest.getConfirmPassword())
                .build();

    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User with id [%s] not found]".formatted(userId)));
    }

    @CacheEvict(value = "userDetails", key = "#userId")
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setProfilePictureUrl(userEditRequest.getProfilePicture());

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new AuthenticationMetaData(user.getId(), username, user.getPassword(), user.getRole());
    }
}

