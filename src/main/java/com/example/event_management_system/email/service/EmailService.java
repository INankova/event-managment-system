package com.example.event_management_system.email.service;

import com.example.event_management_system.email.Client.NotificationClient;
import com.example.event_management_system.email.Client.dto.EventReminderRequest;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.Client.dto.NotificationRequest;
import com.example.event_management_system.email.Client.dto.NotificationScheduleRequest;
import com.example.event_management_system.email.Client.dto.UpsertNotificationPreference;
import com.example.event_management_system.exception.NotificationServiceFeignCallException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final NotificationClient notificationClient;

    @Autowired
    public EmailService(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    public void saveNotification(UUID userId, boolean enabled, String email) {

        UpsertNotificationPreference notification = UpsertNotificationPreference.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .enabled(enabled)
                .build();

        try {
            ResponseEntity<Void> httpResponse = notificationClient.upsertPreference(notification);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Can't save user preference for user with id = [{}]", userId);
            }
        } catch (Exception e) {
            log.error("Unable to call notification-svc while saving user preference. userId={}", userId, e);
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {
        try {
            ResponseEntity<NotificationPreference> httpResponse = notificationClient.getPreference(userId);

            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.warn("Can't get preference for user {}. Status: {}", userId, httpResponse.getStatusCode());
                return null;
            }

            return httpResponse.getBody();
        } catch (FeignException.NotFound e) {
            log.info("No notification preference found for user {} (notification-svc returned 404).", userId);
            return null;
        } catch (Exception e) {
            log.warn("Unable to call notification-svc for getPreference, userId={}", userId, e);
            return null;
        }
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        try {
            ResponseEntity<List<Notification>> httpResponse = notificationClient.getHistory(userId);

            if (!httpResponse.getStatusCode().is2xxSuccessful() || httpResponse.getBody() == null) {
                log.warn("Can't get notification history for user {}. Status: {}", userId, httpResponse.getStatusCode());
                return List.of();
            }

            return httpResponse.getBody();
        } catch (Exception e) {
            log.warn("Unable to call notification-svc for getHistory, userId={}", userId, e);
            return List.of();
        }
    }

    public void sendNotification(UUID userId, String subject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(emailBody)
                .build();

        try {
            ResponseEntity<Void> httpResponse = notificationClient.sendNotification(notificationRequest);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Can't send email to user with id = [{}]", userId);
            }
        } catch (Exception e) {
            log.warn("Can't send email to user with id = [{}].", userId, e);
        }
    }

    public void changeNotificationPreference(UUID userId, boolean enabled) {

        try {
            notificationClient.changeNotificationPreference(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [{}].", userId, e);
        }
    }

    public void clearHistory(UUID userId) {

        try {
            notificationClient.clearHistory(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for clear notification history. userId={}", userId, e);
            throw new NotificationServiceFeignCallException("Can't load");
        }
    }

    @EventListener
    public void onRegisterNotification(RegisterNotificationEvent e) {
        String subject = "Напомняне за събитие: " + e.getEventName();
        String body = "Събитието започва на " + e.getEventStart() + ". Приятно изкарване!";

        scheduleEventReminders(
                e.getUserId(),
                subject,
                body,
                e.getEventStart(),
                List.of(1440, 120)
        );

        log.info("Планирани напомняния за събитие '{}' за потребител {}", e.getEventName(), e.getUserId());
    }

    public void scheduleReminder(UUID userId, String subject, String body, LocalDateTime scheduledAt) {
        NotificationScheduleRequest req = NotificationScheduleRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(body)
                .scheduledAt(scheduledAt)
                .build();
        try {
            ResponseEntity<Void> resp = notificationClient.scheduleReminder(req);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.error("scheduleReminder failed for userId={}, status={}", userId, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("scheduleReminder feign error for userId={}", userId, e);
        }
    }

    public void scheduleEventReminders(UUID userId, String subject, String body,
                                       LocalDateTime eventStart, List<Integer> offsetsMinutes) {
        EventReminderRequest req = EventReminderRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(body)
                .eventStart(eventStart)
                .offsetsMinutes(offsetsMinutes)
                .build();
        try {
            ResponseEntity<Void> resp = notificationClient.scheduleEventReminders(req);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                log.error("scheduleEventReminders failed for userId={}, status={}", userId, resp.getStatusCode());
            }
        } catch (Exception e) {
            log.error("scheduleEventReminders feign error for userId={}", userId, e);
        }
    }
}
