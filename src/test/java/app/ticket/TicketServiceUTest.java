package app.ticket;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.Ticket.service.TicketService;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TicketServiceUTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void cancelTicket_shouldSetStatusCancelled_andSave_whenTicketExistsAndNotCancelled() {
        UUID ticketId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        var owner = mock(User.class);
        when(owner.getId()).thenReturn(ownerId);

        var event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getTitle()).thenReturn("Test Event");

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setOwner(owner);
        ticket.setEvent(event);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.cancelTicket(ticketId);

        assertEquals(TicketStatus.CANCELLED, ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(emailService).scheduleReminder(
                eq(ownerId),
                eq("Your ticket was cancelled"),
                eq("Your ticket for event 'Test Event' was cancelled."),
                any(LocalDateTime.class)
        );
    }

    @Test
    void cancelTicket_shouldThrow_whenTicketAlreadyCancelled() {
        UUID ticketId = UUID.randomUUID();

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setStatus(TicketStatus.CANCELLED);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> ticketService.cancelTicket(ticketId)
        );

        assertEquals("Ticket is already cancelled", ex.getMessage());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void cancelTicket_shouldThrow_whenTicketNotFound() {
        UUID ticketId = UUID.randomUUID();

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> ticketService.cancelTicket(ticketId)
        );

        assertEquals("Ticket not found", ex.getMessage());
        verify(ticketRepository, never()).save(any());
    }
}
