package com.example.event_management_system.web;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import com.example.event_management_system.Event.service.EventService;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @Autowired
    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @DeleteMapping("/delete/{id}")
    public String deleteEvent(@PathVariable UUID id) {

        eventService.deleteEvent(id);

        return "redirect:/profile";
    }

    @GetMapping("/new")
    public ModelAndView getNewEventPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-new-event");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createEventRequest", new CreateEventRequest());
        modelAndView.addObject("eventType", EventType.values());

        return modelAndView;
    }

    @PostMapping("/new")
    public String createEvent(@Valid CreateEventRequest createEventRequest, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "add-new-event";
        }

        User user = userService.getById(authenticationMetaData.getId());

        eventService.createNewEvent(createEventRequest, user);

        return "redirect:/home";
    }

    @PutMapping("/{id}/profile")
    public ModelAndView updateEvent(@PathVariable UUID id, @Valid EventEditRequest eventEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Event event = eventService.getEventById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("update-event");
            modelAndView.addObject("event", event);
            modelAndView.addObject("eventEditRequest", eventEditRequest);
            modelAndView.addObject("eventType", EventType.values());
            return modelAndView;
        }

        eventService.editEventDetails(id, eventEditRequest);

        return new ModelAndView("redirect:/profile");
    }
}
