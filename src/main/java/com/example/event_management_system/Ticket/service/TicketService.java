package com.example.event_management_system.Ticket.service;

import com.example.event_management_system.Ticket.model.Ticket;
import com.example.event_management_system.Ticket.model.TicketStatus;
import com.example.event_management_system.Ticket.repository.TicketRepository;
import com.example.event_management_system.email.service.EmailService;
import com.example.event_management_system.exception.DomainException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class TicketService {
    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    @Autowired
    public TicketService(TicketRepository ticketRepository, EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.emailService = emailService;
    }

    @CacheEvict(value = "ticketCache", key = "#ticketId")
    public void cancelTicket(@PathVariable UUID ticketId) {
        log.info("cancelTicket called for ticketId={}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new DomainException("Ticket not found"));

        log.info("Loaded ticket: id={}, status={}, ownerId={}, eventId={}",
                ticket.getId(),
                ticket.getStatus(),
                ticket.getOwner() != null ? ticket.getOwner().getId() : null,
                ticket.getEvent() != null ? ticket.getEvent().getId() : null
        );

        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            log.warn("Ticket {} is already cancelled", ticketId);
            throw new DomainException("Ticket is already cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);
        log.info("Ticket {} status changed to CANCELLED", ticketId);

        String subject = "Your ticket was cancelled";
        String body = "Your ticket for event '" + ticket.getEvent().getTitle() + "' was cancelled.";

        LocalDateTime scheduledAt = LocalDateTime.now().plusMinutes(1);
        log.info("Scheduling cancellation reminder: userId={}, scheduledAt={}",
                ticket.getOwner().getId(), scheduledAt);

        emailService.scheduleReminder(
                ticket.getOwner().getId(),
                subject,
                body,
                scheduledAt
        );

        log.info("cancelTicket finished for ticketId={}", ticketId);
    }
}

