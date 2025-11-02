package com.example.event_management_system.web;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.web.dto.UserEditRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    @Autowired
    public UserController(UserService userService, TicketRepository ticketRepository, EventRepository eventRepository) {
        this.userService = userService;
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/profile")
    public ModelAndView getProfileMenu(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<Event> userEvents = eventRepository.findByOwnerId(authenticationMetaData.getId());
        List<Ticket> userTickets = ticketRepository.findByOwnerId(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userEvents", userEvents);
        modelAndView.addObject("userTickets", userTickets);

        return modelAndView;
    }

    @GetMapping("/edit-profile")
    public ModelAndView editProfile(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("edit-profile");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/{id}/profile")
    public ModelAndView updateUserProfile(@PathVariable UUID id, @Valid UserEditRequest userEditRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(id);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("profile-menu");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", userEditRequest);
            return modelAndView;
        }

        userService.editUserDetails(id, userEditRequest);

        return new ModelAndView("redirect:/profile");
    }
}
