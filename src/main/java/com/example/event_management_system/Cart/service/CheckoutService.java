package com.example.event_management_system.Cart.service;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.Cart.model.CartItem;
import com.example.event_management_system.Cart.repository.CartRepository;
import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class CheckoutService {

    private final CartRepository cartRepository;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CheckoutService(CartRepository cartRepository,
                           TicketRepository ticketRepository,
                           EventRepository eventRepository,
                           UserRepository userRepository,
                           EmailService emailService) {
        this.cartRepository = cartRepository;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void processCheckout(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("Cart not found for user: " + userId));

        for (CartItem item : cart.getItems()) {
            UUID eventId = item.getEvent().getId();

            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new DomainException("Event not found: " + eventId));

            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setOwner(user);
                ticket.setEvent(event);
                ticket.setPrice(event.getPrice());
                ticket.setStatus(TicketStatus.ACTIVE);
                ticketRepository.save(ticket);
            }
        }

        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        emailService.sendNotification(userId, "Purchase", "You bought tickets.");
    }
}
