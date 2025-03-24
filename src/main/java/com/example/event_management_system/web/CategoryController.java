package com.example.event_management_system.web;
import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import com.example.event_management_system.Event.repository.EventRepository;
import com.example.event_management_system.Security.AuthenticationMetaData;
import com.example.event_management_system.User.model.User;
import com.example.event_management_system.User.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final UserService userService;
    private final EventRepository eventRepository;

    @Autowired
    public CategoryController(UserService userService, EventRepository eventRepository) {
        this.userService = userService;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/sports")
    public ModelAndView getSportsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<Event> sportEvents = eventRepository.findByEventTypeOrderByTitle(EventType.SPORTS);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("sports");
        modelAndView.addObject("user", user);
        modelAndView.addObject("sportEvents", sportEvents);
        return modelAndView;
    }

    @GetMapping("/concerts")
    public ModelAndView getConcertsPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<Event> concertEvents = eventRepository.findByEventTypeOrderByTitle(EventType.CONCERTS);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("concerts");
        modelAndView.addObject("user", user);
        modelAndView.addObject("concertEvents", concertEvents);
        return modelAndView;
    }

    @GetMapping("/cinema")
    public ModelAndView getCinemaPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<Event> cinemaEvents = eventRepository.findByEventTypeOrderByTitle(EventType.CINEMA);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("cinema");
        modelAndView.addObject("user", user);
        modelAndView.addObject("cinemaEvents", cinemaEvents);
        return modelAndView;
    }

    @GetMapping("/theater")
    public ModelAndView getTheaterPage(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        User user = userService.getById(authenticationMetaData.getId());

        List<Event> theaterEvents = eventRepository.findByEventTypeOrderByTitle(EventType.THEATER);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("theater");
        modelAndView.addObject("user", user);
        modelAndView.addObject("theaterEvents", theaterEvents);
        return modelAndView;
    }

}
