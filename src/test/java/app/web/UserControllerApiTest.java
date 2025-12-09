package app.web;

import com.example.event_management_system.event.model.Event;
import com.example.event_management_system.event.repository.EventRepository;
import com.example.event_management_system.security.AuthenticationMetaData;
import com.example.event_management_system.ticket.model.Ticket;
import com.example.event_management_system.ticket.repository.TicketRepository;
import com.example.event_management_system.user.model.Role;
import com.example.event_management_system.user.model.User;
import com.example.event_management_system.user.service.UserService;
import com.example.event_management_system.web.UserController;
import com.example.event_management_system.web.dto.UserEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private UserController userController;

    private AuthenticationMetaData meta(UUID id) {
        return new AuthenticationMetaData(id, "john", "pwd", Role.USER);
    }

    private User user(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        u.setFirstName("John");
        u.setLastName("Doe");
        u.setEmail("john@example.com");
        u.setPhoneNumber("123");
        u.setProfilePictureUrl("pic.png");
        return u;
    }

    @Test
    void getProfileMenu_shouldReturnProfileMenuWithUserEventsAndTickets() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = meta(userId);
        User u = user(userId);

        List<Event> events = List.of(new Event(), new Event());
        List<Ticket> tickets = List.of(new Ticket());

        when(userService.getById(userId)).thenReturn(u);
        when(eventRepository.findByOwnerId(userId)).thenReturn(events);
        when(ticketRepository.findByOwnerId(userId)).thenReturn(tickets);

        ModelAndView mv = userController.getProfileMenu(auth);

        assertEquals("profile-menu", mv.getViewName());
        assertSame(u, mv.getModel().get("user"));
        assertSame(events, mv.getModel().get("userEvents"));
        assertSame(tickets, mv.getModel().get("userTickets"));

        verify(userService).getById(userId);
        verify(eventRepository).findByOwnerId(userId);
        verify(ticketRepository).findByOwnerId(userId);
    }

    @Test
    void editProfile_shouldReturnEditProfileViewWithUserAndUserEditRequest() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = meta(userId);
        User u = user(userId);

        when(userService.getById(userId)).thenReturn(u);

        ModelAndView mv = userController.editProfile(auth);

        assertEquals("edit-profile", mv.getViewName());
        assertSame(u, mv.getModel().get("user"));
        assertTrue(mv.getModel().get("userEditRequest") instanceof UserEditRequest);

        UserEditRequest req = (UserEditRequest) mv.getModel().get("userEditRequest");
        assertEquals(u.getFirstName(), req.getFirstName());
        assertEquals(u.getLastName(), req.getLastName());
        assertEquals(u.getEmail(), req.getEmail());
        assertEquals(u.getPhoneNumber(), req.getPhoneNumber());
        assertEquals(u.getProfilePictureUrl(), req.getProfilePicture());

        verify(userService).getById(userId);
    }

    @Test
    void updateUserProfile_shouldReturnEditProfileView_whenBindingHasErrors() {
        UUID userId = UUID.randomUUID();
        User u = user(userId);

        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(userId)).thenReturn(u);

        UserEditRequest req = new UserEditRequest();
        req.setFirstName("New");
        req.setLastName("Name");

        ModelAndView mv = userController.updateUserProfile(userId, req, bindingResult);

        assertEquals("edit-profile", mv.getViewName());
        assertSame(u, mv.getModel().get("user"));
        assertSame(req, mv.getModel().get("userEditRequest"));

        verify(userService).getById(userId);
        verify(userService, never()).editUserDetails(any(), any());
    }

    @Test
    void updateUserProfile_shouldEditUserAndRedirect_whenValid() {
        UUID userId = UUID.randomUUID();
        UserEditRequest req = new UserEditRequest();

        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mv = userController.updateUserProfile(userId, req, bindingResult);

        assertEquals("redirect:/users/profile", mv.getViewName());
        verify(userService).editUserDetails(userId, req);
    }
}

