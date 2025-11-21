package app.cart;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.Cart.model.CartItem;
import com.example.event_management_system.Cart.repository.CartRepository;
import com.example.event_management_system.Cart.service.CheckoutService;
import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.model.TicketType;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CheckoutServiceUTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CheckoutService checkoutService;

    private UUID userId;
    private User user;
    private Cart cart;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);

        cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void processCheckout_shouldCreateTickets_clearCart_andSendEmail() {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(20))
                .dateTime(LocalDateTime.now())
                .build();

        CartItem item = CartItem.builder()
                .event(event)
                .quantity(2)
                .price(null)
                .build();

        cart.getItems().add(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        checkoutService.processCheckout(userId);

        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository, times(2)).save(ticketCaptor.capture());

        List<Ticket> tickets = ticketCaptor.getAllValues();
        for (Ticket t : tickets) {
            assertEquals(user, t.getOwner());
            assertEquals(event, t.getEvent());
            assertEquals(TicketType.REGULAR, t.getType());
            assertEquals(TicketStatus.ACTIVE, t.getStatus());
            assertEquals(event.getPrice(), t.getPrice());
        }

        assertTrue(cart.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());

        verify(cartRepository).save(cart);
        verify(emailService).sendNotification(userId, "Purchase", "You bought tickets.");
    }

    @Test
    void processCheckout_shouldUseItemPrice_whenProvided() {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(100))
                .dateTime(LocalDateTime.now())
                .build();

        CartItem item = CartItem.builder()
                .event(event)
                .quantity(1)
                .price(BigDecimal.valueOf(60))
                .build();

        cart.getItems().add(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        checkoutService.processCheckout(userId);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());

        Ticket saved = captor.getValue();
        assertEquals(BigDecimal.valueOf(60), saved.getPrice());
    }

    @Test
    void processCheckout_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> checkoutService.processCheckout(userId)
        );

        assertTrue(ex.getMessage().contains("User not found"));
    }

    @Test
    void processCheckout_shouldThrow_whenCartNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.empty());

        DomainException ex = assertThrows(
                DomainException.class,
                () -> checkoutService.processCheckout(userId)
        );

        assertTrue(ex.getMessage().contains("Cart not found"));
    }

    @Test
    void processCheckout_shouldThrow_whenCartIsEmpty() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> checkoutService.processCheckout(userId)
        );

        assertEquals("Cart is empty.", ex.getMessage());
    }

    @Test
    void processCheckout_shouldThrow_whenItemHasNoEvent() {
        CartItem item = new CartItem();
        item.setQuantity(2);
        item.setEvent(null);

        cart.getItems().add(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        DomainException ex = assertThrows(
                DomainException.class,
                () -> checkoutService.processCheckout(userId)
        );

        assertEquals("Cart item has no event.", ex.getMessage());
    }

    @Test
    void processCheckout_shouldSkipItemsWithInvalidQuantity() {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .price(BigDecimal.valueOf(50))
                .build();

        CartItem item = CartItem.builder()
                .event(event)
                .quantity(0)
                .build();

        cart.getItems().add(item);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        checkoutService.processCheckout(userId);

        verify(ticketRepository, never()).save(any());

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);

        verify(emailService).sendNotification(userId, "Purchase", "You bought tickets.");
    }
}

