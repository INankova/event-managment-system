package com.example.event_management_system.Venue.repository;

import com.example.event_management_system.Venue.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {

    boolean existsByName(String name);
}
