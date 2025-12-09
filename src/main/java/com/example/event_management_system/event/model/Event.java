package com.example.event_management_system.event.model;

import com.example.event_management_system.ticket.model.Ticket;
import com.example.event_management_system.user.model.User;
import com.example.event_management_system.venue.model.Venue;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private BigDecimal price;

    private String eventImageUrl;

    @Column(nullable = false)
    private EventType eventType;

    @ManyToOne
    private User owner;

    @ManyToOne
    private Venue venue;

    @OneToMany(mappedBy = "event", fetch = FetchType.EAGER)
    private Set<Ticket> tickets = new HashSet<>();

}
