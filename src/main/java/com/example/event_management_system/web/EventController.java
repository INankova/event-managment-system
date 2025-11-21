package com.example.event_management_system.web;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import com.example.event_management_system.Event.service.EventService;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.dto.CreateEventRequest;
import com.example.event_management_system.web.dto.EventEditRequest;
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

        return "redirect:/users/profile";
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
    public ModelAndView createEvent(
            @Valid @ModelAttribute("createEventRequest") CreateEventRequest createEventRequest,
            BindingResult bindingResult,
            @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("add-new-event");
            modelAndView.addObject("user", user);
            modelAndView.addObject("createEventRequest", createEventRequest);
            modelAndView.addObject("eventType", EventType.values());
            return modelAndView;
        }

        eventService.createNewEvent(createEventRequest, user);
        return new ModelAndView("redirect:/home");
    }



    @GetMapping("/update/{id}")
    public ModelAndView showUpdateEventPage(@PathVariable UUID id) {
        Event event = eventService.getEventById(id);

        ModelAndView modelAndView = new ModelAndView("update-event");
        modelAndView.addObject("eventEditRequest", new EventEditRequest(event));
        modelAndView.addObject("eventType", EventType.values());

        return modelAndView;
    }

    @PutMapping("/{id}/update")
    public ModelAndView updateEvent(@PathVariable UUID id,
                                    @Valid @ModelAttribute("eventEditRequest") EventEditRequest eventEditRequest,
                                    BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("update-event");
            modelAndView.addObject("eventEditRequest", eventEditRequest);
            modelAndView.addObject("eventType", EventType.values());
            return modelAndView;
        }

        eventService.editEventDetails(id, eventEditRequest);

        return new ModelAndView("redirect:/users/profile");
    }

}
