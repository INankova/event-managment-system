package com.example.event_management_system.Cart.repository;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.User.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserId(UUID userId);

}
