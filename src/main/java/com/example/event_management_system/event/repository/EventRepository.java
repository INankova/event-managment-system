package com.example.event_management_system.event.repository;

import com.example.event_management_system.event.model.Event;
import com.example.event_management_system.event.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByEventTypeOrderByTitle(EventType eventType);
    List<Event> findByOwnerId(UUID userId);
    List<Event> findByDateTimeBefore(LocalDateTime time);
}
