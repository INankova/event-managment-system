package com.example.event_management_system.User.service;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.Role;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import com.example.event_management_system.web.dto.RegisterRequest;
import com.example.event_management_system.web.dto.UserEditRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest registerRequest) {

        Optional<User> optionalUser = userRepository.findByUsernameOrEmail(registerRequest.getUsername(), registerRequest.getEmail());
        if (optionalUser.isPresent()) {
            throw new UsernameNotFoundException("Username already exists");
        }

        User user = userRepository.save(initializeUser(registerRequest));

        log.info("User created: {}", user.getUsername());

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
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .confirmPassword(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .build();

    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getById(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new DomainException("User with id [%s] not found]".formatted(userId)));
    }

    @Transactional
    @CacheEvict(value = "userDetails", key = "#userId")
    public void editUserDetails(UUID userId, UserEditRequest userEditRequest) {

        User user = getById(userId);

        if (userEditRequest.getEmail().isBlank()) {
            emailService.saveNotification(userId, false, null);
        }

        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
        user.setEmail(userEditRequest.getEmail());
        user.setProfilePictureUrl(userEditRequest.getProfilePicture());
        user.setPhoneNumber(userEditRequest.getPhoneNumber());

        boolean wantsPasswordChange = userEditRequest.getNewPassword() != null && !userEditRequest.getNewPassword().isBlank();
        if (wantsPasswordChange) {
            if (userEditRequest.getCurrentPassword() == null || userEditRequest.getCurrentPassword().isBlank()) {
                throw new DomainException("Current password is required to change password.");
            }
            if (!passwordEncoder.matches(userEditRequest.getCurrentPassword(), user.getPassword())) {
                throw new DomainException("Current password is not correct.");
            }
            if (userEditRequest.getConfirmNewPassword() == null ||
                    !userEditRequest.getNewPassword().equals(userEditRequest.getConfirmNewPassword())) {
                throw new DomainException("New passwords do not match.");
            }

            user.setPassword(passwordEncoder.encode(userEditRequest.getNewPassword()));
            user.setConfirmPassword(user.getPassword());
        }

//        if (!userEditRequest.getEmail().isBlank()) {
//            emailService.saveNotification(userId, true, userEditRequest.getEmail());
//        }

        userRepository.save(user);

        AuthenticationMetaData updated = new AuthenticationMetaData(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole()
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        updated, null, updated.getAuthorities()
                )
        );
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new AuthenticationMetaData(user.getId(), username, user.getPassword(), user.getRole());
    }
}

