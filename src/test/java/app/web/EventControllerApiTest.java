package app.web;

import com.example.event_management_system.event.model.Event;
import com.example.event_management_system.event.model.EventType;
import com.example.event_management_system.event.service.EventService;
import com.example.event_management_system.security.AuthenticationMetaData;
import com.example.event_management_system.user.model.Role;
import com.example.event_management_system.user.model.User;
import com.example.event_management_system.user.service.UserService;
import com.example.event_management_system.web.EventController;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerApiTest {

    @Mock
    private EventService eventService;

    @Mock
    private UserService userService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private EventController eventController;

    private AuthenticationMetaData meta(UUID id) {
        return new AuthenticationMetaData(id, "john", "pwd", Role.USER);
    }

    private User mockUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        return u;
    }

    @Test
    void deleteEvent_shouldCallServiceAndRedirect() {
        UUID eventId = UUID.randomUUID();

        String result = eventController.deleteEvent(eventId);

        verify(eventService).deleteEvent(eventId);
        assertEquals("redirect:/users/profile", result);
    }

    @Test
    void getNewEventPage_shouldReturnModelAndViewWithUserAndTypes() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = meta(userId);
        User user = mockUser(userId);

        when(userService.getById(userId)).thenReturn(user);

        ModelAndView mv = eventController.getNewEventPage(meta);

        assertEquals("add-new-event", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertNotNull(mv.getModel().get("createEventRequest"));
        assertArrayEquals(EventType.values(), (Object[]) mv.getModel().get("eventType"));

        verify(userService).getById(userId);
    }

    @Test
    void createEvent_shouldReturnView_whenBindingHasErrors() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = meta(userId);
        User user = mockUser(userId);
        CreateEventRequest req = new CreateEventRequest();

        when(bindingResult.hasErrors()).thenReturn(true);
        when(userService.getById(userId)).thenReturn(user);

        ModelAndView mv = eventController.createEvent(req, bindingResult, meta);

        assertEquals("add-new-event", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertSame(req, mv.getModel().get("createEventRequest"));
        assertArrayEquals(EventType.values(), (Object[]) mv.getModel().get("eventType"));
        verifyNoInteractions(eventService);
    }

    @Test
    void createEvent_shouldSaveEventAndRedirect_whenValid() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = meta(userId);
        User user = mockUser(userId);
        CreateEventRequest req = new CreateEventRequest();

        when(bindingResult.hasErrors()).thenReturn(false);
        when(userService.getById(userId)).thenReturn(user);

        ModelAndView mv = eventController.createEvent(req, bindingResult, meta);

        verify(eventService).createNewEvent(req, user);
        assertEquals("redirect:/home", mv.getViewName());
    }

    @Test
    void showUpdateEventPage_shouldReturnViewWithEventEditRequest() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event();

        when(eventService.getEventById(eventId)).thenReturn(event);

        ModelAndView mv = eventController.showUpdateEventPage(eventId);

        assertEquals("update-event", mv.getViewName());
        assertTrue(mv.getModel().get("eventEditRequest") instanceof EventEditRequest);
        assertArrayEquals(EventType.values(), (Object[]) mv.getModel().get("eventType"));

        verify(eventService).getEventById(eventId);
    }

    @Test
    void updateEvent_shouldReturnSameView_whenBindingErrors() {
        UUID eventId = UUID.randomUUID();
        EventEditRequest request = new EventEditRequest();

        when(bindingResult.hasErrors()).thenReturn(true);

        ModelAndView mv = eventController.updateEvent(eventId, request, bindingResult);

        assertEquals("update-event", mv.getViewName());
        assertSame(request, mv.getModel().get("eventEditRequest"));
        assertArrayEquals(EventType.values(), (Object[]) mv.getModel().get("eventType"));
        verify(eventService, never()).editEventDetails(any(), any());
    }

    @Test
    void updateEvent_shouldUpdateEventAndRedirect_whenValid() {
        UUID eventId = UUID.randomUUID();
        EventEditRequest request = new EventEditRequest();

        when(bindingResult.hasErrors()).thenReturn(false);

        ModelAndView mv = eventController.updateEvent(eventId, request, bindingResult);

        verify(eventService).editEventDetails(eventId, request);
        assertEquals("redirect:/users/profile", mv.getViewName());
    }
}
