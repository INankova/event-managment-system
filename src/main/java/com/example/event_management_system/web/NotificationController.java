package com.example.event_management_system.web;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.email.Client.dto.Notification;
import com.example.event_management_system.email.Client.dto.NotificationPreference;
import com.example.event_management_system.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public NotificationController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping
    public ModelAndView getNotificationPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        NotificationPreference notificationPreference = emailService.getNotificationPreference(user.getId());
        List<Notification> notificationHistory = emailService.getNotificationHistory(user.getId());
        notificationHistory = notificationHistory.stream().limit(3).toList();

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("notificationPreference", notificationPreference);
        modelAndView.addObject("notificationHistory", notificationHistory);

        return modelAndView;
    }
}
