package app.event;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Event.service.EventService;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.exception.DomainException;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceUTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getEventById_shouldReturnEvent_whenExists() {
        UUID id = UUID.randomUUID();
        Event event = Event.builder()
                .id(id)
                .title("Concert")
                .build();

        when(eventRepository.findById(id)).thenReturn(Optional.of(event));

        Event result = eventService.getEventById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Concert", result.getTitle());
        verify(eventRepository).findById(id);
    }

    @Test
    void getEventById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(eventRepository.findById(id)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> eventService.getEventById(id)
        );

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(eventRepository).findById(id);
    }

    @Test
    void deleteEvent_shouldCallRepositoryDeleteById() {
        UUID id = UUID.randomUUID();

        eventService.deleteEvent(id);

        verify(eventRepository).deleteById(id);
    }

    @Test
    void editEventDetails_shouldUpdateFieldsAndSave() {
        UUID eventId = UUID.randomUUID();

        Event existingEvent = Event.builder()
                .id(eventId)
                .title("Old title")
                .description("Old description")
                .location("Old location")
                .price(BigDecimal.valueOf(10))
                .dateTime(LocalDateTime.of(2024, 1, 1, 18, 0))
                .eventImageUrl("old.png")
                .eventType(EventType.CONCERTS)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        EventEditRequest request = new EventEditRequest();
        request.setTitle("New title");
        request.setDescription("New description");
        request.setLocation("New location");
        request.setPrice(BigDecimal.valueOf(25));
        request.setDateTime(LocalDateTime.of(2025, 5, 5, 20, 30));
        request.setImageUrl("new.png");
        request.setEventType(EventType.SPORTS);

        eventService.editEventDetails(eventId, request);

        assertEquals("New title", existingEvent.getTitle());
        assertEquals("New description", existingEvent.getDescription());
        assertEquals("New location", existingEvent.getLocation());
        assertEquals(BigDecimal.valueOf(25), existingEvent.getPrice());
        assertEquals(LocalDateTime.of(2025, 5, 5, 20, 30), existingEvent.getDateTime());
        assertEquals("new.png", existingEvent.getEventImageUrl());
        assertEquals(EventType.SPORTS, existingEvent.getEventType());


        verify(eventRepository).save(existingEvent);
    }

    @Test
    void createNewEvent_shouldBuildEventFromRequestAndUser_andSave() {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("New event");
        request.setDescription("Some description");
        request.setLocation("Sofia");
        request.setPrice(BigDecimal.valueOf(50));
        request.setDateTime(LocalDateTime.of(2025, 6, 1, 19, 0));
        request.setImageUrl("img.png");
        request.setEventType(EventType.CONCERTS);

        User owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setUsername("john");

        eventService.createNewEvent(request, owner);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event saved = eventCaptor.getValue();
        assertEquals("New event", saved.getTitle());
        assertEquals("Some description", saved.getDescription());
        assertEquals("Sofia", saved.getLocation());
        assertEquals(BigDecimal.valueOf(50), saved.getPrice());
        assertEquals(LocalDateTime.of(2025, 6, 1, 19, 0), saved.getDateTime());
        assertEquals("img.png", saved.getEventImageUrl());
        assertEquals(EventType.CONCERTS, saved.getEventType());
        assertEquals(owner, saved.getOwner());
    }

    @Test
    void findEventsBefore_shouldDelegateToRepository() {
        LocalDateTime time = LocalDateTime.now();

        Event e1 = Event.builder().id(UUID.randomUUID()).build();
        Event e2 = Event.builder().id(UUID.randomUUID()).build();

        when(eventRepository.findByDateTimeBefore(time)).thenReturn(List.of(e1, e2));

        List<Event> result = eventService.findEventsBefore(time);

        assertEquals(2, result.size());
        assertSame(e1, result.get(0));
        assertSame(e2, result.get(1));
        verify(eventRepository).findByDateTimeBefore(time);
    }
}

