package app.email;

import com.example.event_management_system.email.Client.NotificationClient;
import com.example.event_management_system.email.Client.dto.EventReminderRequest;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.Client.dto.NotificationRequest;
import com.example.event_management_system.email.Client.dto.NotificationScheduleRequest;
import com.example.event_management_system.email.Client.dto.UpsertNotificationPreference;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.NotificationServiceFeignCallException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceUTest {

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private EmailService emailService;

    @Test
    void saveNotification_shouldCallUpsertPreferenceWithCorrectDto_whenClientReturns2xx() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.upsertPreference(any(UpsertNotificationPreference.class)))
                .thenReturn(ResponseEntity.ok().build());

        emailService.saveNotification(userId, true, "user@example.com");

        ArgumentCaptor<UpsertNotificationPreference> captor =
                ArgumentCaptor.forClass(UpsertNotificationPreference.class);
        verify(notificationClient).upsertPreference(captor.capture());

        UpsertNotificationPreference sent = captor.getValue();
        assertEquals(userId, sent.getUserId());
        assertEquals("user@example.com", sent.getContactInfo());
        assertEquals("EMAIL", sent.getType());
        assertTrue(sent.isEnabled());
    }

    @Test
    void saveNotification_shouldSwallowExceptions_whenClientThrows() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.upsertPreference(any()))
                .thenThrow(new RuntimeException("feign down"));

        assertDoesNotThrow(() ->
                emailService.saveNotification(userId, true, "user@example.com"));
    }

    @Test
    void getNotificationPreference_shouldReturnBody_when2xx() {
        UUID userId = UUID.randomUUID();

        NotificationPreference pref = NotificationPreference.builder()
                .type("EMAIL")
                .enabled(true)
                .contactInfo("user@example.com")
                .build();

        when(notificationClient.getPreference(userId))
                .thenReturn(ResponseEntity.ok(pref));

        NotificationPreference result = emailService.getNotificationPreference(userId);

        assertNotNull(result);
        assertEquals("EMAIL", result.getType());
        assertTrue(result.isEnabled());
        assertEquals("user@example.com", result.getContactInfo());
    }


    @Test
    void getNotificationPreference_shouldReturnNull_whenStatusNot2xx() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.getPreference(userId))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        NotificationPreference result = emailService.getNotificationPreference(userId);

        assertNull(result);
    }

    @Test
    void getNotificationPreference_shouldReturnNull_whenFeignNotFound() {
        UUID userId = UUID.randomUUID();

        FeignException.NotFound notFound = mock(FeignException.NotFound.class);
        when(notificationClient.getPreference(userId)).thenThrow(notFound);

        NotificationPreference result = emailService.getNotificationPreference(userId);

        assertNull(result);
    }

    @Test
    void getNotificationPreference_shouldReturnNull_whenOtherException() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.getPreference(userId))
                .thenThrow(new RuntimeException("boom"));

        NotificationPreference result = emailService.getNotificationPreference(userId);

        assertNull(result);
    }

    @Test
    void getNotificationHistory_shouldReturnBody_when2xxAndBodyNotNull() {
        UUID userId = UUID.randomUUID();
        List<Notification> list = List.of(new Notification(), new Notification());

        when(notificationClient.getHistory(userId))
                .thenReturn(ResponseEntity.ok(list));

        List<Notification> result = emailService.getNotificationHistory(userId);

        assertEquals(2, result.size());
    }

    @Test
    void getNotificationHistory_shouldReturnEmptyList_whenStatusNot2xxOrBodyNull() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.getHistory(userId))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        List<Notification> result1 = emailService.getNotificationHistory(userId);
        assertTrue(result1.isEmpty());

        when(notificationClient.getHistory(userId))
                .thenReturn(ResponseEntity.ok(null));

        List<Notification> result2 = emailService.getNotificationHistory(userId);
        assertTrue(result2.isEmpty());
    }

    @Test
    void getNotificationHistory_shouldReturnEmptyList_whenExceptionThrown() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.getHistory(userId))
                .thenThrow(new RuntimeException("boom"));

        List<Notification> result = emailService.getNotificationHistory(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void sendNotification_shouldCallClientWithBuiltRequest_whenNoException() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        emailService.sendNotification(userId, "Subj", "Body");

        ArgumentCaptor<NotificationRequest> captor =
                ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationClient).sendNotification(captor.capture());

        NotificationRequest req = captor.getValue();
        assertEquals(userId, req.getUserId());
        assertEquals("Subj", req.getSubject());
        assertEquals("Body", req.getBody());
    }

    @Test
    void sendNotification_shouldSwallowException_whenClientThrows() {
        UUID userId = UUID.randomUUID();

        when(notificationClient.sendNotification(any()))
                .thenThrow(new RuntimeException("feign down"));

        assertDoesNotThrow(() ->
                emailService.sendNotification(userId, "Subj", "Body"));
    }

    @Test
    void changeNotificationPreference_shouldCallClient() {
        UUID userId = UUID.randomUUID();

        emailService.changeNotificationPreference(userId, true);

        verify(notificationClient).changeNotificationPreference(userId, true);
    }

    @Test
    void changeNotificationPreference_shouldSwallowException_whenClientThrows() {
        UUID userId = UUID.randomUUID();

        doThrow(new RuntimeException("boom"))
                .when(notificationClient).changeNotificationPreference(userId, false);

        assertDoesNotThrow(() ->
                emailService.changeNotificationPreference(userId, false));
    }

    @Test
    void clearHistory_shouldCallClient() {
        UUID userId = UUID.randomUUID();

        emailService.clearHistory(userId);

        verify(notificationClient).clearHistory(userId);
    }

    @Test
    void clearHistory_shouldThrowNotificationServiceFeignCallException_whenClientThrows() {
        UUID userId = UUID.randomUUID();

        doThrow(new RuntimeException("down"))
                .when(notificationClient).clearHistory(userId);

        assertThrows(NotificationServiceFeignCallException.class,
                () -> emailService.clearHistory(userId));
    }

    @Test
    void scheduleReminder_shouldCallClientWithBuiltRequest() {
        UUID userId = UUID.randomUUID();
        LocalDateTime scheduledAt = LocalDateTime.now().plusHours(1);

        when(notificationClient.scheduleReminder(any(NotificationScheduleRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        emailService.scheduleReminder(userId, "Subj", "Body", scheduledAt);

        ArgumentCaptor<NotificationScheduleRequest> captor =
                ArgumentCaptor.forClass(NotificationScheduleRequest.class);
        verify(notificationClient).scheduleReminder(captor.capture());

        NotificationScheduleRequest req = captor.getValue();
        assertEquals(userId, req.getUserId());
        assertEquals("Subj", req.getSubject());
        assertEquals("Body", req.getBody());
        assertEquals(scheduledAt, req.getScheduledAt());
    }

    @Test
    void scheduleReminder_shouldSwallowException_whenClientThrows() {
        UUID userId = UUID.randomUUID();
        LocalDateTime scheduledAt = LocalDateTime.now().plusHours(1);

        when(notificationClient.scheduleReminder(any()))
                .thenThrow(new RuntimeException("feign down"));

        assertDoesNotThrow(() ->
                emailService.scheduleReminder(userId, "Subj", "Body", scheduledAt));
    }

    @Test
    void scheduleEventReminders_shouldBuildRequestWithOffsetsAndCallClient() {
        UUID userId = UUID.randomUUID();
        LocalDateTime eventStart = LocalDateTime.now().plusDays(1);
        List<Integer> offsets = List.of(1440, 120);

        when(notificationClient.scheduleEventReminders(any(EventReminderRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        emailService.scheduleEventReminders(userId, "Event", "Body", eventStart, offsets);

        ArgumentCaptor<EventReminderRequest> captor =
                ArgumentCaptor.forClass(EventReminderRequest.class);
        verify(notificationClient).scheduleEventReminders(captor.capture());

        EventReminderRequest req = captor.getValue();
        assertEquals(userId, req.getUserId());
        assertEquals("Event", req.getSubject());
        assertEquals("Body", req.getBody());
        assertEquals(eventStart, req.getEventStart());
        assertEquals(offsets, req.getOffsetsMinutes());
    }

    @Test
    void scheduleEventReminders_shouldSwallowException_whenClientThrows() {
        UUID userId = UUID.randomUUID();
        LocalDateTime eventStart = LocalDateTime.now().plusDays(1);

        when(notificationClient.scheduleEventReminders(any()))
                .thenThrow(new RuntimeException("feign down"));

        assertDoesNotThrow(() ->
                emailService.scheduleEventReminders(
                        userId, "Event", "Body", eventStart, List.of(60, 30)));
    }

    @Test
    void onRegisterNotification_shouldScheduleDefaultEventReminders() {
        EmailService spyService = Mockito.spy(new EmailService(notificationClient));

        RegisterNotificationEvent event = RegisterNotificationEvent.builder()
                .userId(UUID.randomUUID())
                .email("user@example.com")
                .eventName("Party")
                .eventStart(LocalDateTime.of(2030, 1, 1, 20, 0))
                .build();

        doNothing().when(spyService).scheduleEventReminders(
                any(UUID.class),
                anyString(),
                anyString(),
                any(LocalDateTime.class),
                anyList()
        );

        spyService.onRegisterNotification(event);

        verify(spyService).scheduleEventReminders(
                eq(event.getUserId()),
                eq("Event Reminder: " + event.getEventName()),
                eq("The event starts on " + event.getEventStart() + ". Enjoy!"),
                eq(event.getEventStart()),
                eq(List.of(1440, 120))
        );
    }
}

