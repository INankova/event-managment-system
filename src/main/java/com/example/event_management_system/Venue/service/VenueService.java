package com.example.event_management_system.Venue.service;

import com.example.event_management_system.Venue.model.Venue;
import com.example.event_management_system.Venue.repository.VenueRepository;
import com.example.event_management_system.web.dto.CreateVenueRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {
    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @CacheEvict(value = "venues", allEntries = true)
    public void createNewVenue(CreateVenueRequest createVenueRequest) {

        Venue venue = Venue.builder()
                .name(createVenueRequest.getName())
                .address(createVenueRequest.getAddress())
                .city(createVenueRequest.getCity())
                .capacity(createVenueRequest.getCapacity())
                .contactInfo(createVenueRequest.getContactInfo())
                .build();

        venueRepository.save(venue);
    }

    @Cacheable("venues")
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }
}
