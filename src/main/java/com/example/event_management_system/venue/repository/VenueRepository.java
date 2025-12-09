package com.example.event_management_system.venue.repository;

import com.example.event_management_system.venue.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {

    boolean existsByName(String name);
}
