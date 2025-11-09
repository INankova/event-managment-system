package com.example.event_management_system.Cart.service;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.Cart.model.CartItem;
import com.example.event_management_system.Cart.repository.CartRepository;
import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.model.TicketType;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.repository.UserRepository;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class CheckoutService {

    private final CartRepository cartRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public CheckoutService(CartRepository cartRepository,
                           TicketRepository ticketRepository,
                           UserRepository userRepository,
                           EmailService emailService) {
        this.cartRepository = cartRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void processCheckout(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException("User not found: " + userId));

        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new DomainException("Cart not found for user: " + userId));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new DomainException("Cart is empty.");
        }

        for (CartItem item : cart.getItems()) {
            if (item.getEvent() == null) {
                throw new DomainException("Cart item has no event.");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                continue;
            }

            BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : item.getEvent().getPrice();

            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setOwner(user);
                ticket.setEvent(item.getEvent());
                ticket.setType(TicketType.REGULAR);
                ticket.setStatus(TicketStatus.ACTIVE);
                ticket.setDateTime(item.getEvent().getDateTime());
                ticket.setPrice(unitPrice);
                ticketRepository.save(ticket);
            }
        }


        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        // Изпращаме известие
        emailService.sendNotification(userId, "Purchase", "You bought tickets.");
    }
}
