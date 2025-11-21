package app.user;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.Role;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import com.example.event_management_system.web.dto.RegisterRequest;
import com.example.event_management_system.web.dto.UserEditRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldThrowException_whenUserAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userRepository.findByUsernameOrEmail("john", "john@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void register_shouldCreateUserAndPublishEvent_whenDataIsValid() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userRepository.findByUsernameOrEmail("john", "john@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UUID userId = UUID.randomUUID();
        User savedUser = User.builder()
                .id(userId)
                .username("john")
                .email("john@example.com")
                .password("encodedPassword")
                .confirmPassword("encodedPassword")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.register(request);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("john", result.getUsername());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(Role.USER, result.getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();
        assertEquals("john", userToSave.getUsername());
        assertEquals("john@example.com", userToSave.getEmail());
        assertEquals("encodedPassword", userToSave.getPassword());
        assertEquals("encodedPassword", userToSave.getConfirmPassword());
        assertEquals(Role.USER, userToSave.getRole());

        verify(eventPublisher).publishEvent(any(RegisterNotificationEvent.class));
    }

    @Test
    void editUserDetails_shouldThrow_whenCurrentPasswordIsNull() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserEditRequest request = new UserEditRequest();
        request.setEmail("new@example.com");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("newPassword");
        request.setCurrentPassword(null);

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.editUserDetails(userId, request)
        );

        assertEquals("Current password is required to change password.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void editUserDetails_shouldThrow_whenConfirmNewPasswordIsNull() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        UserEditRequest request = new UserEditRequest();
        request.setEmail("new@example.com");
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword(null);

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.editUserDetails(userId, request)
        );

        assertEquals("New passwords do not match.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id)
                .username("john")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("john", result.getUsername());
    }

    @Test
    void getById_shouldThrowDomainException_whenUserNotFound() {
        UUID id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.getById(id)
        );

        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    void editUserDetails_shouldUpdateUserAndSecurityContext_whenDataIsValidAndPasswordChanges() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .firstName("OldFirst")
                .lastName("OldLast")
                .email("old@example.com")
                .profilePictureUrl("old-pic")
                .phoneNumber("111111")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        UserEditRequest request = new UserEditRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");
        request.setEmail("new@example.com");
        request.setProfilePicture("new-pic");
        request.setPhoneNumber("222222");

        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("newPassword");

        userService.editUserDetails(userId, request);

        assertEquals("NewFirst", existingUser.getFirstName());
        assertEquals("NewLast", existingUser.getLastName());
        assertEquals("new@example.com", existingUser.getEmail());
        assertEquals("new-pic", existingUser.getProfilePictureUrl());
        assertEquals("222222", existingUser.getPhoneNumber());
        assertEquals("encodedNewPassword", existingUser.getPassword());
        assertEquals("encodedNewPassword", existingUser.getConfirmPassword());

        verify(userRepository).save(existingUser);

        verify(emailService).saveNotification(userId, true, "new@example.com");
        verify(emailService, never()).saveNotification(userId, false, null);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getPrincipal() instanceof AuthenticationMetaData);
        AuthenticationMetaData meta = (AuthenticationMetaData) auth.getPrincipal();
        assertEquals(userId, meta.getId());
        assertEquals("john", meta.getUsername());
        assertEquals("encodedNewPassword", meta.getPassword());
        assertEquals(Role.USER, meta.getRole());
    }

    @Test
    void editUserDetails_shouldNotTriggerPasswordValidation_whenNewPasswordIsNull() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .email("old@example.com")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserEditRequest request = new UserEditRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");
        request.setEmail("new@example.com");
        request.setProfilePicture("pic");
        request.setPhoneNumber("999999");

        request.setNewPassword(null);
        request.setConfirmNewPassword(null);
        request.setCurrentPassword(null);

        // act
        userService.editUserDetails(userId, request);

        assertEquals("encodedOldPassword", existingUser.getPassword());
        assertEquals("encodedOldPassword", existingUser.getConfirmPassword());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());


        verify(userRepository).save(existingUser);


        verify(emailService).saveNotification(userId, true, "new@example.com");
    }

    @Test
    void editUserDetails_shouldHandleBlankEmailAndNotChangePassword_whenNoNewPassword() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .email("old@example.com")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserEditRequest request = new UserEditRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");
        request.setEmail("");
        request.setProfilePicture("pic");
        request.setPhoneNumber("999999");
        request.setNewPassword("");

        userService.editUserDetails(userId, request);

        assertEquals("", existingUser.getEmail());
        assertEquals("NewFirst", existingUser.getFirstName());
        assertEquals("NewLast", existingUser.getLastName());
        assertEquals("pic", existingUser.getProfilePictureUrl());
        assertEquals("999999", existingUser.getPhoneNumber());

        assertEquals("encodedOldPassword", existingUser.getPassword());
        assertEquals("encodedOldPassword", existingUser.getConfirmPassword());

        verify(userRepository).save(existingUser);

        verify(emailService).saveNotification(userId, false, null);
        verify(emailService, never()).saveNotification(eq(userId), eq(true), anyString());
    }

    @Test
    void editUserDetails_shouldThrow_whenCurrentPasswordMissing() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserEditRequest request = new UserEditRequest();
        request.setEmail("new@example.com");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("newPassword");
        request.setCurrentPassword("");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.editUserDetails(userId, request)
        );

        assertEquals("Current password is required to change password.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void editUserDetails_shouldThrow_whenCurrentPasswordNotCorrect() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        UserEditRequest request = new UserEditRequest();
        request.setEmail("new@example.com");
        request.setCurrentPassword("wrongPassword");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("newPassword");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.editUserDetails(userId, request)
        );

        assertEquals("Current password is not correct.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void editUserDetails_shouldThrow_whenNewPasswordsDoNotMatch() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        UserEditRequest request = new UserEditRequest();
        request.setEmail("new@example.com");
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        request.setConfirmNewPassword("differentPassword");

        DomainException ex = assertThrows(
                DomainException.class,
                () -> userService.editUserDetails(userId, request)
        );

        assertEquals("New passwords do not match.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_shouldReturnAuthenticationMetaData_whenUserExists() {
        String username = "john";
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username(username)
                .password("encodedPassword")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof AuthenticationMetaData);
        AuthenticationMetaData meta = (AuthenticationMetaData) userDetails;
        assertEquals(userId, meta.getId());
        assertEquals(username, meta.getUsername());
        assertEquals("encodedPassword", meta.getPassword());
        assertEquals(Role.ADMIN, meta.getRole());
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserDoesNotExist() {
        String username = "missing";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username)
        );
    }

    @Test
    void getAllUsers_shouldReturnUsersFromRepository() {
        User u1 = User.builder().id(UUID.randomUUID()).username("u1").build();
        User u2 = User.builder().id(UUID.randomUUID()).username("u2").build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("u1", result.get(0).getUsername());
        assertEquals("u2", result.get(1).getUsername());
    }

    @Test
    void editUserDetails_shouldNotTriggerPasswordValidation_whenNewPasswordIsBlankOrNull() {
        UUID userId = UUID.randomUUID();

        User existingUser = User.builder()
                .id(userId)
                .username("john")
                .password("encodedOldPassword")
                .confirmPassword("encodedOldPassword")
                .email("old@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        UserEditRequest request = new UserEditRequest();
        request.setFirstName("NewFirst");
        request.setLastName("NewLast");

        request.setNewPassword("   ");
        request.setConfirmNewPassword(null);
        request.setCurrentPassword(null);

        request.setEmail("new@example.com");
        request.setProfilePicture("pic");
        request.setPhoneNumber("111111");

        userService.editUserDetails(userId, request);

        assertEquals("encodedOldPassword", existingUser.getPassword());

        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());

        verify(userRepository).save(existingUser);

        verify(emailService).saveNotification(userId, true, "new@example.com");
    }
}
