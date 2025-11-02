package com.example.event_management_system.Event.repository;

import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByEventTypeOrderByTitle(EventType eventType);
    List<Event> findByUserId(UUID userId);
    List<Event> findByEndDateBefore(LocalDateTime time);
    List<Event> findAllByDateTimeBetween(LocalDateTime from, LocalDateTime to);
}
