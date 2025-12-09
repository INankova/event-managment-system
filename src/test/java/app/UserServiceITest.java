package app;

import com.example.event_management_system.Application;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.Role;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.exception.UsernameAlreadyExistsException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import com.example.event_management_system.web.dto.RegisterRequest;
import com.example.event_management_system.web.dto.UserEditRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = Application.class)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
@Import(UserServiceITest.TestConfig.class)
class UserServiceITest {

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public ApplicationEventPublisher applicationEventPublisher() {
            return mock(ApplicationEventPublisher.class);
        }

        @Bean
        @Primary
        public EmailService emailService() {
            return mock(EmailService.class);
        }
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void resetMocks() {
        reset(eventPublisher, emailService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void contextLoads_andUserServiceExists() {
        assertNotNull(userService);
    }

    @Test
    void register_shouldPersistUser_andPublishEvent_andSaveNotificationPref() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("secret");

        User u = userService.register(req);

        assertNotNull(u.getId());
        assertEquals("john", u.getUsername());
        assertEquals("john@example.com", u.getEmail());
        assertEquals(Role.USER, u.getRole());

        assertNotEquals("secret", u.getPassword());
        assertTrue(passwordEncoder.matches("secret", u.getPassword()));

        User fromDb = userRepository.findByUsername("john").orElseThrow();
        assertEquals(u.getId(), fromDb.getId());

        ArgumentCaptor<RegisterNotificationEvent> eventCaptor =
                ArgumentCaptor.forClass(RegisterNotificationEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        RegisterNotificationEvent event = eventCaptor.getValue();
        assertEquals(u.getId(), event.getUserId());
        assertEquals("john@example.com", event.getEmail());

        verify(emailService, times(1))
                .saveNotification(u.getId(), true, "john@example.com");
    }

    @Test
    void register_shouldThrow_whenUsernameOrEmailAlreadyExist() {
        userRepository.save(
                User.builder()
                        .username("john")
                        .email("john@example.com")
                        .password(passwordEncoder.encode("pass"))
                        .confirmPassword(passwordEncoder.encode("pass"))
                        .role(Role.USER)
                        .build()
        );

        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("secret");

        assertThrows(UsernameAlreadyExistsException.class,
                () -> userService.register(req));

        verifyNoInteractions(emailService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void getAllUsers_shouldReturnAllUsersFromRepository() {
        User u1 = userRepository.save(
                User.builder()
                        .username("u1")
                        .email("u1@example.com")
                        .password(passwordEncoder.encode("p"))
                        .confirmPassword(passwordEncoder.encode("p"))
                        .role(Role.USER)
                        .build()
        );

        User u2 = userRepository.save(
                User.builder()
                        .username("u2")
                        .email("u2@example.com")
                        .password(passwordEncoder.encode("p"))
                        .confirmPassword(passwordEncoder.encode("p"))
                        .role(Role.ADMIN)
                        .build()
        );

        List<User> users = userService.getAllUsers();

        assertTrue(users.size() >= 2);
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("u1")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("u2")));
    }



    @Test
    void getById_shouldReturnUser_whenExists() {
        User saved = userRepository.save(
                User.builder()
                        .username("maria")
                        .email("maria@example.com")
                        .password(passwordEncoder.encode("pass"))
                        .confirmPassword(passwordEncoder.encode("pass"))
                        .role(Role.USER)
                        .build()
        );

        User fromService = userService.getById(saved.getId());

        assertEquals(saved.getId(), fromService.getId());
        assertEquals("maria", fromService.getUsername());
    }

    @Test
    void getById_shouldThrowDomainException_whenNotFound() {
        UUID randomId = UUID.randomUUID();

        assertThrows(DomainException.class,
                () -> userService.getById(randomId));
    }

    @Test
    void editUserDetails_shouldUpdateUserAndSecurityContext_whenPasswordChanges_andEmailNotificationIsSent() {
        User user = User.builder()
                .username("maria")
                .email("maria@example.com")
                .password(passwordEncoder.encode("oldpass"))
                .confirmPassword(passwordEncoder.encode("oldpass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Maria");
        req.setLastName("Petrova");
        req.setEmail("maria.new@example.com");
        req.setProfilePicture("pic.png");
        req.setPhoneNumber("123456");
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass");
        req.setConfirmNewPassword("newpass");

        userService.editUserDetails(userId, req);

        User updated = userRepository.findById(userId).orElseThrow();

        assertEquals("Maria", updated.getFirstName());
        assertEquals("Petrova", updated.getLastName());
        assertEquals("maria.new@example.com", updated.getEmail());
        assertEquals("pic.png", updated.getProfilePictureUrl());
        assertEquals("123456", updated.getPhoneNumber());
        assertTrue(passwordEncoder.matches("newpass", updated.getPassword()));

        verify(emailService, times(1))
                .saveNotification(userId, true, "maria.new@example.com");

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth instanceof UsernamePasswordAuthenticationToken);
        assertTrue(auth.getPrincipal() instanceof AuthenticationMetaData);

        AuthenticationMetaData meta = (AuthenticationMetaData) auth.getPrincipal();
        assertEquals(userId, meta.getId());
        assertEquals("maria", meta.getUsername());
        assertEquals(updated.getPassword(), meta.getPassword());
        assertEquals(Role.USER, meta.getRole());
    }

    @Test
    void editUserDetails_shouldUpdateWithoutPasswordChange_whenNewPasswordBlank_andEmailNotBlank() {
        User user = User.builder()
                .username("anna")
                .email("anna@example.com")
                .password(passwordEncoder.encode("oldpass"))
                .confirmPassword(passwordEncoder.encode("oldpass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Anna");
        req.setLastName("Petrova");
        req.setEmail("anna.new@example.com");
        req.setProfilePicture("pic.png");
        req.setPhoneNumber("777777");
        req.setCurrentPassword(null);
        req.setNewPassword("");
        req.setConfirmNewPassword(null);

        String oldPasswordHash = user.getPassword();

        userService.editUserDetails(userId, req);

        User updated = userRepository.findById(userId).orElseThrow();

        assertEquals("Anna", updated.getFirstName());
        assertEquals("Petrova", updated.getLastName());
        assertEquals("anna.new@example.com", updated.getEmail());
        assertEquals("pic.png", updated.getProfilePictureUrl());
        assertEquals("777777", updated.getPhoneNumber());
        assertEquals(oldPasswordHash, updated.getPassword());

        verify(emailService, times(1))
                .saveNotification(userId, true, "anna.new@example.com");
        verify(emailService, never())
                .saveNotification(userId, false, null);
    }

    @Test
    void editUserDetails_shouldSendNotificationWithDisabled_whenEmailIsBlank() {
        User user = User.builder()
                .username("ivan")
                .email("ivan@example.com")
                .password(passwordEncoder.encode("pass"))
                .confirmPassword(passwordEncoder.encode("pass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Ivan");
        req.setLastName("Ivanov");
        req.setEmail("");
        req.setProfilePicture("pic2.png");
        req.setPhoneNumber("999999");
        req.setCurrentPassword(null);
        req.setNewPassword(null);
        req.setConfirmNewPassword(null);

        userService.editUserDetails(userId, req);

        User updated = userRepository.findById(userId).orElseThrow();

        assertEquals("", updated.getEmail());
        assertEquals("Ivan", updated.getFirstName());
        assertEquals("Ivanov", updated.getLastName());
        assertEquals("pic2.png", updated.getProfilePictureUrl());
        assertEquals("999999", updated.getPhoneNumber());

        verify(emailService, times(1))
                .saveNotification(userId, false, null);
        verify(emailService, never())
                .saveNotification(userId, true, "");
    }

    @Test
    void editUserDetails_shouldThrow_whenCurrentPasswordIsWrong() {
        User user = User.builder()
                .username("gosho")
                .email("gosho@example.com")
                .password(passwordEncoder.encode("oldpass"))
                .confirmPassword(passwordEncoder.encode("oldpass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Gosho");
        req.setLastName("Goshev");
        req.setEmail("gosho.new@example.com");
        req.setProfilePicture("pic3.png");
        req.setPhoneNumber("555555");
        req.setCurrentPassword("wrongpass");
        req.setNewPassword("newpass");
        req.setConfirmNewPassword("newpass");

        assertThrows(DomainException.class,
                () -> userService.editUserDetails(userId, req));
    }

    @Test
    void editUserDetails_shouldThrow_whenCurrentPasswordMissingButWantsPasswordChange() {
        User user = User.builder()
                .username("niki")
                .email("niki@example.com")
                .password(passwordEncoder.encode("oldpass"))
                .confirmPassword(passwordEncoder.encode("oldpass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Niki");
        req.setLastName("Nikolaev");
        req.setEmail("niki.new@example.com");
        req.setProfilePicture("pic.png");
        req.setPhoneNumber("111111");
        req.setCurrentPassword(null);
        req.setNewPassword("newpass");
        req.setConfirmNewPassword("newpass");

        assertThrows(DomainException.class,
                () -> userService.editUserDetails(userId, req));
    }

    @Test
    void editUserDetails_shouldThrow_whenNewPasswordsDoNotMatch() {
        User user = User.builder()
                .username("dani")
                .email("dani@example.com")
                .password(passwordEncoder.encode("oldpass"))
                .confirmPassword(passwordEncoder.encode("oldpass"))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        UUID userId = user.getId();

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("Dani");
        req.setLastName("Danova");
        req.setEmail("dani.new@example.com");
        req.setProfilePicture("pic.png");
        req.setPhoneNumber("222222");
        req.setCurrentPassword("oldpass");
        req.setNewPassword("newpass1");
        req.setConfirmNewPassword("newpass2");

        assertThrows(DomainException.class,
                () -> userService.editUserDetails(userId, req));
    }

    @Test
    void loadUserByUsername_shouldReturnAuthenticationMetaData_whenUserExists() {
        User saved = userRepository.save(
                User.builder()
                        .username("pesho")
                        .email("pesho@example.com")
                        .password(passwordEncoder.encode("pass"))
                        .confirmPassword(passwordEncoder.encode("pass"))
                        .role(Role.ADMIN)
                        .build()
        );

        var userDetails = userService.loadUserByUsername("pesho");

        assertTrue(userDetails instanceof AuthenticationMetaData);
        AuthenticationMetaData meta = (AuthenticationMetaData) userDetails;
        assertEquals(saved.getId(), meta.getId());
        assertEquals("pesho", meta.getUsername());
        assertEquals(saved.getPassword(), meta.getPassword());
        assertEquals(Role.ADMIN, meta.getRole());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing-user"));
    }
}


