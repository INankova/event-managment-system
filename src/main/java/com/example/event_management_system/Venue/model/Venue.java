package com.example.event_management_system.Venue.model;

import com.example.event_management_system.Event.model.Event;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private Integer capacity;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String contactInfo;

    @OneToMany(mappedBy = "venue", fetch = FetchType.EAGER)
    private ArrayList<Event> events = new ArrayList<>();
}
