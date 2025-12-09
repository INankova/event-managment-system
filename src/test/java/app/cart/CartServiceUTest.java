package app.cart;

import com.example.event_management_system.cart.model.Cart;
import com.example.event_management_system.cart.model.CartItem;
import com.example.event_management_system.cart.repository.CartRepository;
import com.example.event_management_system.cart.service.CartService;
import com.example.event_management_system.event.model.Event;
import com.example.event_management_system.event.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CartServiceUTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addToCart_shouldAddNewItem_whenNotExisting() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).price(BigDecimal.valueOf(10)).build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.addToCart(userId, eventId);

        assertEquals(1, cart.getItems().size());
        assertEquals(eventId, cart.getItems().get(0).getEvent().getId());
        assertEquals(1, cart.getItems().get(0).getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void addToCart_shouldIncreaseQuantity_whenItemExists() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).price(BigDecimal.valueOf(10)).build();

        CartItem existing = CartItem.builder()
                .event(event)
                .quantity(2)
                .build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(existing)));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.addToCart(userId, eventId);

        assertEquals(3, existing.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void addToCart_shouldThrow_whenEventNotFound() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addToCart(userId, eventId));
    }

    @Test
    void increaseQuantity_shouldIncrease_whenItemExists() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).build();

        CartItem item = CartItem.builder()
                .event(event)
                .quantity(1)
                .build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.increaseQuantity(userId, eventId);

        assertEquals(2, item.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void increaseQuantity_shouldThrow_whenCartNotFound() {
        UUID userId = UUID.randomUUID();

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cartService.increaseQuantity(userId, UUID.randomUUID()));
    }

    @Test
    void increaseQuantity_shouldThrow_whenItemNotFound() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.increaseQuantity(userId, eventId));
    }

    @Test
    void decreaseQuantity_shouldDecrease_whenQuantityGreaterThan1() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).build();

        CartItem item = CartItem.builder()
                .event(event)
                .quantity(3)
                .build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.decreaseQuantity(userId, eventId);

        assertEquals(2, item.getQuantity());
        verify(cartRepository).save(cart);
    }

    @Test
    void decreaseQuantity_shouldRemoveItem_whenQuantityIs1() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).build();
        CartItem item = CartItem.builder()
                .event(event)
                .quantity(1)
                .build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.decreaseQuantity(userId, eventId);

        assertTrue(cart.getItems().isEmpty());
        assertNull(item.getCart()); // should disconnect
        verify(cartRepository).save(cart);
    }

    @Test
    void decreaseQuantity_shouldThrow_whenItemNotFound() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>());

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.decreaseQuantity(userId, eventId));
    }

    @Test
    void removeItem_shouldDeleteItemFromCart() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        Event event = Event.builder().id(eventId).build();
        CartItem item = CartItem.builder()
                .event(event)
                .quantity(2)
                .build();

        Cart cart = new Cart();
        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        cartService.removeItem(userId, eventId);

        assertTrue(cart.getItems().isEmpty());
        assertNull(item.getCart());
        verify(cartRepository).save(cart);
    }

    @Test
    void getShoppingCart_shouldReturnExistingCart() {
        UUID userId = UUID.randomUUID();

        Cart cart = new Cart();
        cart.setUserId(userId);

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.of(cart));

        Cart result = cartService.getShoppingCart(userId);

        assertSame(cart, result);
    }

    @Test
    void getShoppingCart_shouldCreateNewCartIfNotExists() {
        UUID userId = UUID.randomUUID();

        Cart newCart = new Cart();
        newCart.setUserId(userId);

        when(cartRepository.findWithItemsByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart result = cartService.getShoppingCart(userId);

        assertEquals(userId, result.getUserId());
    }

    @Test
    void calculateTotalPrice_shouldReturnZero_whenCartEmpty() {
        Cart c = new Cart();
        c.setItems(Collections.emptyList());

        assertEquals(BigDecimal.ZERO, cartService.calculateTotalPrice(c));
    }

    @Test
    void calculateTotalPrice_shouldWorkWithEventPrice() {
        Event e = Event.builder().price(BigDecimal.valueOf(10)).build();

        CartItem item = CartItem.builder()
                .event(e)
                .quantity(2)
                .build();

        Cart cart = new Cart();
        cart.setItems(List.of(item));

        assertEquals(BigDecimal.valueOf(20), cartService.calculateTotalPrice(cart));
    }

    @Test
    void calculateTotalPrice_shouldWorkWithCustomItemPrice() {
        Event e = Event.builder().price(BigDecimal.valueOf(10)).build();

        CartItem item = CartItem.builder()
                .event(e)
                .price(BigDecimal.valueOf(7))
                .quantity(3)
                .build();

        Cart cart = new Cart();
        cart.setItems(List.of(item));

        assertEquals(BigDecimal.valueOf(21), cartService.calculateTotalPrice(cart));
    }
}
