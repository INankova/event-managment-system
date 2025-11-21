package com.example.event_management_system.email.Client;

import com.example.event_management_system.email.Client.dto.EventReminderRequest;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.Client.dto.NotificationRequest;
import com.example.event_management_system.email.Client.dto.NotificationScheduleRequest;
import com.example.event_management_system.email.Client.dto.UpsertNotificationPreference;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notification-service", url = "http://localhost:8081/api/v1/notifications")
public interface NotificationClient {

    @PostMapping("/preferences")
    ResponseEntity<Void> upsertPreference(@RequestBody UpsertNotificationPreference upsertNotificationPreference);

    @GetMapping("/preferences")
    ResponseEntity<NotificationPreference> getPreference(@RequestParam(name = "userId") UUID userId);

    @GetMapping
    ResponseEntity<List<Notification>> getHistory(@RequestParam(name = "userId") UUID userId);

    @PostMapping
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest notificationRequest);

    @PutMapping("/preferences")
    ResponseEntity<Void> changeNotificationPreference(@RequestParam("userId") UUID userId, @RequestParam("enabled") boolean enabled);

    @DeleteMapping
    ResponseEntity<Void> clearHistory(@RequestParam(name = "userId") UUID userId);

    @PostMapping("/reminders/schedule")
    ResponseEntity<Void> scheduleReminder(@RequestBody NotificationScheduleRequest request);

    @PostMapping("/reminders/event")
    ResponseEntity<Void> scheduleEventReminders(@RequestBody EventReminderRequest request);
}
