package com.example.event_management_system.User.repository;

import com.example.event_management_system.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
}
