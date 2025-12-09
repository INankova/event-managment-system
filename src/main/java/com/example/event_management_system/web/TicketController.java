package com.example.event_management_system.web;

import com.example.event_management_system.ticket.model.Ticket;
import com.example.event_management_system.ticket.repository.TicketRepository;
import com.example.event_management_system.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketController(TicketService ticketService, TicketRepository ticketRepository) {
        this.ticketService = ticketService;
        this.ticketRepository = ticketRepository;
    }

    @PostMapping("/{ticketId}/cancel")
    public String cancelTicket(@PathVariable UUID ticketId) {
        ticketService.cancelTicket(ticketId);
        return "redirect:/users/profile";
    }

    @GetMapping
    public ModelAndView showTickets(UUID userId) {
        List<Ticket> tickets = ticketRepository.findByOwnerId(userId);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("profile-menu");
        modelAndView.addObject("tickets", tickets);
        return modelAndView;
    }

}
