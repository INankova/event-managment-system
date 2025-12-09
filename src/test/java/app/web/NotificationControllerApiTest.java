package app.web;

import com.example.event_management_system.security.AuthenticationMetaData;
import com.example.event_management_system.user.model.User;
import com.example.event_management_system.user.service.UserService;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.web.NotificationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerApiTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationController notificationController;


    private AuthenticationMetaData meta(UUID id) {
        return new AuthenticationMetaData(id, "john", "", null);
    }

    private User mockUser(UUID id) {
        User u = new User();
        u.setId(id);
        u.setUsername("john");
        return u;
    }

    @Test
    void getNotificationPage_shouldReturnModelWithUserPreferenceAndSucceededNotifications() {

        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = meta(userId);

        User user = mockUser(userId);

        NotificationPreference pref = new NotificationPreference();

        // История
        Notification n1 = new Notification();
        n1.setStatus("SUCCEEDED");

        Notification n2 = new Notification();
        n2.setStatus("FAILED");

        Notification n3 = new Notification();
        n3.setStatus("SUCCEEDED");

        when(userService.getById(userId)).thenReturn(user);
        when(emailService.getNotificationPreference(userId)).thenReturn(pref);
        when(emailService.getNotificationHistory(userId))
                .thenReturn(List.of(n1, n2, n3));

        ModelAndView mv = notificationController.getNotificationPage(auth);

        assertSame(user, mv.getModel().get("user"));
        assertSame(pref, mv.getModel().get("notificationPreference"));

        @SuppressWarnings("unchecked")
        List<Notification> filtered =
                (List<Notification>) mv.getModel().get("notificationHistory");

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(n -> n.getStatus().equals("SUCCEEDED")));

        verify(userService).getById(userId);
        verify(emailService).getNotificationPreference(userId);
        verify(emailService).getNotificationHistory(userId);
    }

    @Test
    void updateUserPreference_shouldCallServiceAndRedirect() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = meta(userId);

        String result = notificationController.updateUserPreference(true, auth);

        verify(emailService).changeNotificationPreference(userId, true);
        assertEquals("redirect:/notifications", result);
    }

    @Test
    void deleteNotificationHistory_shouldCallServiceAndRedirect() {
        UUID userId = UUID.randomUUID();
        AuthenticationMetaData auth = meta(userId);

        String result = notificationController.deleteNotificationHistory(auth);

        verify(emailService).clearHistory(userId);
        assertEquals("redirect:/notifications", result);
    }
}

