package app.web;

import com.example.event_management_system.security.AuthenticationMetaData;
import com.example.event_management_system.user.model.Role;
import com.example.event_management_system.user.model.User;
import com.example.event_management_system.user.service.UserService;
import com.example.event_management_system.venue.model.Venue;
import com.example.event_management_system.venue.service.VenueService;
import com.example.event_management_system.web.VenueController;
import com.example.event_management_system.web.dto.CreateVenueRequest;
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
class VenueControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private VenueService venueService;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private VenueController venueController;

    private AuthenticationMetaData adminMeta(UUID id) {
        return new AuthenticationMetaData(id, "admin", "pwd", Role.ADMIN);
    }

    private User adminUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("admin");
        u.setRole(Role.ADMIN);
        return u;
    }

    @Test
    void getVenuesPage_shouldReturnVenuesViewWithVenuesList() {
        Venue v1 = new Venue();
        Venue v2 = new Venue();
        List<Venue> venues = List.of(v1, v2);

        when(venueService.getAllVenues()).thenReturn(venues);

        ModelAndView mv = venueController.getVenuesPage();

        assertEquals("venues", mv.getViewName());
        assertSame(venues, mv.getModel().get("venues"));
        verify(venueService).getAllVenues();
    }

    @Test
    void addNewVenue_shouldReturnAddNewVenueViewWithUserAndCreateVenueRequest() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData meta = adminMeta(userId);
        User admin = adminUser(userId);

        when(userService.getById(userId)).thenReturn(admin);

        ModelAndView mv = venueController.addNewVenue(meta);

        assertEquals("add-new-venue", mv.getViewName());
        assertSame(admin, mv.getModel().get("user"));
        assertTrue(mv.getModel().get("createVenueRequest") instanceof CreateVenueRequest);

        verify(userService).getById(userId);
    }

    @Test
    void createNewVenue_shouldReturnAddNewVenueView_whenBindingHasErrors() {
        when(bindingResult.hasErrors()).thenReturn(true);

        CreateVenueRequest req = new CreateVenueRequest();

        String view = venueController.createNewVenue(req, bindingResult);

        assertEquals("add-new-venue", view);
        verify(venueService, never()).createNewVenue(any());
    }

    @Test
    void createNewVenue_shouldCallServiceAndRedirect_whenValid() {
        when(bindingResult.hasErrors()).thenReturn(false);

        CreateVenueRequest req = new CreateVenueRequest();
        req.setName("Arena Armeec");
        req.setCity("Sofia");
        req.setAddress("Bul. Asen Yordanov");
        req.setCapacity(15000);
        req.setContactInfo("contact@arena.bg");

        String view = venueController.createNewVenue(req, bindingResult);

        assertEquals("redirect:/home", view);
        verify(venueService).createNewVenue(req);
    }
}

