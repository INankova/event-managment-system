package app.web;

import com.example.event_management_system.ticket.model.Ticket;
import com.example.event_management_system.ticket.repository.TicketRepository;
import com.example.event_management_system.ticket.service.TicketService;
import com.example.event_management_system.web.TicketController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerApiTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketController ticketController;

    @Test
    void cancelTicket_shouldCallServiceAndRedirect() {
        UUID ticketId = UUID.randomUUID();

        String view = ticketController.cancelTicket(ticketId);

        assertEquals("redirect:/users/profile", view);
        verify(ticketService).cancelTicket(ticketId);
    }

    @Test
    void showTickets_shouldReturnTicketsInModel() {
        UUID userId = UUID.randomUUID();

        Ticket t1 = new Ticket();
        Ticket t2 = new Ticket();
        List<Ticket> tickets = List.of(t1, t2);

        when(ticketRepository.findByOwnerId(userId)).thenReturn(tickets);

        ModelAndView mv = ticketController.showTickets(userId);

        assertEquals("profile-menu", mv.getViewName());
        assertSame(tickets, mv.getModel().get("tickets"));

        verify(ticketRepository).findByOwnerId(userId);
    }
}
