package app.venue;

import com.example.event_management_system.venue.model.Venue;
import com.example.event_management_system.venue.repository.VenueRepository;
import com.example.event_management_system.venue.service.VenueService;
import com.example.event_management_system.exception.VenueNameAlreadyExistsException;
import com.example.event_management_system.web.dto.CreateVenueRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class VenueServiceUTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    void createNewVenue_shouldThrow_whenNameAlreadyExists() {
        CreateVenueRequest request = new CreateVenueRequest();
        request.setName("Arena Sofia");

        when(venueRepository.existsByName("Arena Sofia")).thenReturn(true);

        VenueNameAlreadyExistsException ex = assertThrows(
                VenueNameAlreadyExistsException.class,
                () -> venueService.createNewVenue(request)
        );

        assertEquals("Venue with name 'Arena Sofia' already exists", ex.getMessage());
        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    void createNewVenue_shouldSaveVenue_whenNameDoesNotExist() {
        CreateVenueRequest request = new CreateVenueRequest();
        request.setName("Arena Sofia");
        request.setAddress("Some street 1");
        request.setCity("Sofia");
        request.setCapacity(10000);
        request.setContactInfo("phone/email");

        when(venueRepository.existsByName("Arena Sofia")).thenReturn(false);

        venueService.createNewVenue(request);

        ArgumentCaptor<Venue> venueCaptor = ArgumentCaptor.forClass(Venue.class);
        verify(venueRepository).save(venueCaptor.capture());

        Venue saved = venueCaptor.getValue();
        assertEquals("Arena Sofia", saved.getName());
        assertEquals("Some street 1", saved.getAddress());
        assertEquals("Sofia", saved.getCity());
        assertEquals(10000, saved.getCapacity());
        assertEquals("phone/email", saved.getContactInfo());
    }

    @Test
    void getAllVenues_shouldReturnVenuesFromRepository() {
        Venue v1 = Venue.builder().name("A").build();
        Venue v2 = Venue.builder().name("B").build();

        when(venueRepository.findAll()).thenReturn(List.of(v1, v2));

        List<Venue> result = venueService.getAllVenues();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
        verify(venueRepository).findAll();
    }
}
