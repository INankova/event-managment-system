package com.example.event_management_system.Ticket.service;

import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.exception.DomainException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Service
@Transactional
public class TicketService {
    private final TicketRepository ticketRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @CacheEvict(value = "ticketCache", key = "#ticketId")
    public void cancelTicket(@PathVariable UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new DomainException("Ticket not found"));

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new DomainException("Ticket is already cancelled");
        }


        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
    }

}
