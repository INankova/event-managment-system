package app.web;

import com.example.event_management_system.cart.model.Cart;
import com.example.event_management_system.cart.service.CartService;
import com.example.event_management_system.cart.service.CheckoutService;
import com.example.event_management_system.security.AuthenticationMetaData;
import com.example.event_management_system.user.model.Role;
import com.example.event_management_system.web.CartController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerApiTest {

    @Mock
    private CartService cartService;

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CartController cartController;

    private AuthenticationMetaData buildAuth(UUID userId) {
        return new AuthenticationMetaData(
                userId,
                "john",
                "pwd",
                Role.USER
        );
    }

    @Test
    void shoppingCart_shouldReturnShoppingCartViewWithCartTotalAndUser() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        Cart cart = new Cart();
        BigDecimal total = BigDecimal.valueOf(42);

        when(cartService.getShoppingCart(userId)).thenReturn(cart);
        when(cartService.calculateTotalPrice(cart)).thenReturn(total);

        ModelAndView mv = cartController.shoppingCart(auth);

        assertEquals("shopping-cart", mv.getViewName());
        assertSame(cart, mv.getModel().get("cart"));
        assertEquals(total, mv.getModel().get("totalPrice"));
        assertSame(auth, mv.getModel().get("user"));

        verify(cartService).getShoppingCart(userId);
        verify(cartService).calculateTotalPrice(cart);
    }

    @Test
    void addToCart_shouldCallServiceAndRedirectToHome() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        String view = cartController.addToCart(eventId, auth);

        assertEquals("redirect:/home", view);
        verify(cartService).addToCart(userId, eventId);
    }

    @Test
    void increaseQuantity_shouldCallServiceAndRedirectToCart() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        String view = cartController.increaseQuantity(eventId, auth);

        assertEquals("redirect:/cart", view);
        verify(cartService).increaseQuantity(userId, eventId);
    }

    @Test
    void decreaseQuantity_shouldCallServiceAndRedirectToCart() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        String view = cartController.decreaseQuantity(eventId, auth);

        assertEquals("redirect:/cart", view);
        verify(cartService).decreaseQuantity(userId, eventId);
    }

    @Test
    void removeItem_shouldCallServiceAndRedirectToCart() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        String view = cartController.removeItem(eventId, auth);

        assertEquals("redirect:/cart", view);
        verify(cartService).removeItem(userId, eventId);
    }

    @Test
    void checkout_shouldCallCheckoutServiceAndRedirectToProfile() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = buildAuth(userId);

        String view = cartController.checkout(auth);

        assertEquals("redirect:/users/profile", view);
        verify(checkoutService).processCheckout(userId);
    }
}

