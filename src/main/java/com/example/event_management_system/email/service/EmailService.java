package com.example.event_management_system.email.service;

import com.example.event_management_system.email.Client.NotificationClient;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.Client.dto.NotificationRequest;
import com.example.event_management_system.email.Client.dto.UpsertNotificationPreference;
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

    @EventListener
    public void SendRegisterNotif(RegisterNotificationEvent event) {
        System.out.println("Welcome, you are registered!");
    }

    public void saveNotification(UUID userId, boolean enabled, String email) {

        UpsertNotificationPreference notification = UpsertNotificationPreference.builder()
                .userId(userId)
                .contactInfo(email)
                .type("EMAIL")
                .enabled(enabled)
                .build();

        ResponseEntity<Void> httpResponse = notificationClient.upsertPreference(notification);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Can't save preference for user " + userId);
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

        ResponseEntity<Notification> httpResponse = notificationClient.sendNotification(notificationRequest);

        if (!httpResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Can't send email to user " + userId);
        }
    }
}
