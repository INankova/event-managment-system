package app.web;

import com.example.event_management_system.Cart.service.CartService;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.IndexController;
import com.example.event_management_system.web.dto.LoginRequest;
import com.example.event_management_system.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private IndexController indexController;

    @Test
    void getIndexPage_shouldReturnIndexView() {
        String view = indexController.getIndexPage();
        assertEquals("index", view);
    }

    @Test
    void getLoginPage_shouldReturnLoginViewAndLoginRequest_whenNoError() {
        ModelAndView mv = indexController.getLoginPage(null);

        assertEquals("login", mv.getViewName());
        assertTrue(mv.getModel().containsKey("loginRequest"));
        assertInstanceOf(LoginRequest.class, mv.getModel().get("loginRequest"));
        assertFalse(mv.getModel().containsKey("errorMessage"));
    }

    @Test
    void getLoginPage_shouldAddErrorMessage_whenErrorParamPresent() {
        ModelAndView mv = indexController.getLoginPage("true");

        assertEquals("login", mv.getViewName());
        assertTrue(mv.getModel().containsKey("loginRequest"));
        assertEquals("Incorrect username or password!", mv.getModel().get("errorMessage"));
    }

    @Test
    void getHomePage_shouldLoadUserAndReturnHomeView() {
        UUID userId = UUID.randomUUID();

        AuthenticationMetaData auth = new AuthenticationMetaData(
                userId,
                "john",
                "pwd",
                null
        );

        User user = User.builder()
                .id(userId)
                .username("john")
                .build();

        when(userService.getById(userId)).thenReturn(user);
        when(cartService.getItemsCount(userId)).thenReturn(3);

        ModelAndView mv = indexController.getHomePage(auth);

        assertEquals("home", mv.getViewName());
        assertTrue(mv.getModel().containsKey("user"));
        assertTrue(mv.getModel().containsKey("cartCount"));

        assertSame(user, mv.getModel().get("user"));
        assertEquals(3, mv.getModel().get("cartCount"));

        verify(userService).getById(userId);
        verify(cartService).getItemsCount(userId);
    }


    @Test
    void getRegisterPage_shouldReturnRegisterViewWithRegisterRequest() {
        ModelAndView mv = indexController.getRegisterPage();

        assertEquals("register", mv.getViewName());
        assertTrue(mv.getModel().containsKey("registerRequest"));
        assertInstanceOf(RegisterRequest.class, mv.getModel().get("registerRequest"));
    }

    @Test
    void registerNewUser_shouldReturnRegisterView_whenBindingHasErrors() {
        RegisterRequest req = new RegisterRequest();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(true);

        String view = indexController.registerNewUser(req, bindingResult);

        assertEquals("register", view);
        verify(userService, never()).register(any());
    }

    @Test
    void registerNewUser_shouldRegisterUserAndRedirectToLogin_whenNoErrors() {
        RegisterRequest req = new RegisterRequest();
        BindingResult bindingResult = mock(BindingResult.class);

        when(bindingResult.hasErrors()).thenReturn(false);

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .build();

        when(userService.register(req)).thenReturn(user);

        String view = indexController.registerNewUser(req, bindingResult);

        assertEquals("redirect:/login", view);
        verify(userService).register(req);
    }
}

