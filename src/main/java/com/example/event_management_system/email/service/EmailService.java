package com.example.event_management_system.email.service;

import com.example.event_management_system.email.Client.NotificationClient;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.Client.dto.NotificationRequest;
import com.example.event_management_system.email.Client.dto.UpsertNotificationPreference;
import com.example.event_management_system.exception.NotificationServiceFeignCallException;
import com.example.event_management_system.web.dto.RegisterNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
                log.error("[Feign call to notification-svc failed] Can't save user preference for user with id = [%s]".formatted(userId));
            }
        } catch (Exception e) {
            log.error("Unable to call notification-svc.");
        }
    }

    public NotificationPreference getNotificationPreference(UUID userId) {

       ResponseEntity<NotificationPreference> httpResponse = notificationClient.getPreference(userId);

       if (!httpResponse.getStatusCode().is2xxSuccessful()) {
           throw new RuntimeException("Can't get preference for user " + userId);
       }

       return httpResponse.getBody();
    }

    public List<Notification> getNotificationHistory(UUID userId) {
        ResponseEntity<List<Notification>> httpResponse = notificationClient.getHistory(userId);


        return httpResponse.getBody();

    }

    public void sendNotification(UUID userId, String subject, String emailBody) {

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .subject(subject)
                .body(emailBody)
                .build();

        ResponseEntity<Void> httpResponse;
        try {
            httpResponse = notificationClient.sendNotification(notificationRequest);
            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                log.error("[Feign call to notification-svc failed] Can't send email to user with id = [%s]".formatted(userId));
            }
        } catch (Exception e) {
            log.warn("Can't send email to user with id = [%s] due to 500 Internal Server Error.".formatted(userId));
        }
    }

    public void changeNotificationPreference(UUID userId, boolean enabled) {

        try {
            notificationClient.changeNotificationPreference(userId, enabled);
        } catch (Exception e) {
            log.warn("Can't update notification preferences for user with id = [%s].".formatted(userId));
        }
    }

    public void clearHistory(UUID userId) {

        try {
            notificationClient.clearHistory(userId);
        } catch (Exception e) {
            log.error("Unable to call notification-svc for clear notification history.".formatted(userId));
            throw new NotificationServiceFeignCallException("Can't load");
        }
    }
}
