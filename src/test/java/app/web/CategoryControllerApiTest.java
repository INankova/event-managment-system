package app.web;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.Role;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.CategoryController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryController categoryController;

    private AuthenticationMetaData auth(UUID id) {
        return new AuthenticationMetaData(id, "john", "pwd", Role.USER);
    }

    private User mockUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        return u;
    }

    @Test
    void getSportsPage_shouldReturnSportsViewAndEvents() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = auth(userId);

        User user = mockUser(userId);
        List<Event> events = List.of(new Event(), new Event());

        when(userService.getById(userId)).thenReturn(user);
        when(eventRepository.findByEventTypeOrderByTitle(EventType.SPORTS)).thenReturn(events);

        ModelAndView mv = categoryController.getSportsPage(meta);

        assertEquals("sports", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertSame(events, mv.getModel().get("sportEvents"));

        verify(userService).getById(userId);
        verify(eventRepository).findByEventTypeOrderByTitle(EventType.SPORTS);
    }

    @Test
    void getConcertsPage_shouldReturnConcertsViewAndEvents() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = auth(userId);

        User user = mockUser(userId);
        List<Event> events = List.of(new Event());

        when(userService.getById(userId)).thenReturn(user);
        when(eventRepository.findByEventTypeOrderByTitle(EventType.CONCERTS)).thenReturn(events);

        ModelAndView mv = categoryController.getConcertsPage(meta);

        assertEquals("concerts", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertSame(events, mv.getModel().get("concertEvents"));

        verify(userService).getById(userId);
        verify(eventRepository).findByEventTypeOrderByTitle(EventType.CONCERTS);
    }

    @Test
    void getCinemaPage_shouldReturnCinemaViewAndEvents() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = auth(userId);

        User user = mockUser(userId);
        List<Event> events = List.of(new Event());

        when(userService.getById(userId)).thenReturn(user);
        when(eventRepository.findByEventTypeOrderByTitle(EventType.CINEMA)).thenReturn(events);

        ModelAndView mv = categoryController.getCinemaPage(meta);

        assertEquals("cinema", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertSame(events, mv.getModel().get("cinemaEvents"));

        verify(userService).getById(userId);
        verify(eventRepository).findByEventTypeOrderByTitle(EventType.CINEMA);
    }

    @Test
    void getTheaterPage_shouldReturnTheaterViewAndEvents() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = auth(userId);

        User user = mockUser(userId);
        List<Event> events = List.of(new Event(), new Event(), new Event());

        when(userService.getById(userId)).thenReturn(user);
        when(eventRepository.findByEventTypeOrderByTitle(EventType.THEATER)).thenReturn(events);

        ModelAndView mv = categoryController.getTheaterPage(meta);

        assertEquals("theater", mv.getViewName());
        assertSame(user, mv.getModel().get("user"));
        assertSame(events, mv.getModel().get("theaterEvents"));

        verify(userService).getById(userId);
        verify(eventRepository).findByEventTypeOrderByTitle(EventType.THEATER);
    }
}

