package com.example.event_management_system.web;

import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import com.example.event_management_system.Venue.model.Venue;
import com.example.event_management_system.Venue.service.VenueService;
import com.example.event_management_system.web.dto.CreateVenueRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/venues")
public class VenueController {

    private final UserService userService;
    private final VenueService venueService;

    @Autowired
    public VenueController(UserService userService, VenueService venueService) {
        this.userService = userService;
        this.venueService = venueService;
    }

    @GetMapping
    public ModelAndView getVenuesPage() {



        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("venues");

        List<Venue> venues = venueService.getAllVenues();

        modelAndView.addObject("venues", venues);

        return modelAndView;
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView addNewVenue(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-new-venue");
        modelAndView.addObject("user", user);
        modelAndView.addObject("createVenueRequest", new CreateVenueRequest());

        return modelAndView;
    }

    @PostMapping("/new")
    public String createNewVenue(@Valid CreateVenueRequest createVenueRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "add-new-venue";
        }

        venueService.createNewVenue(createVenueRequest);

        return "redirect:/home";
    }

}
