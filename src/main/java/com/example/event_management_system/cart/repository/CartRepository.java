package com.example.event_management_system.cart.repository;

import com.example.event_management_system.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("""
        select distinct c
        from Cart c
        left join fetch c.items i
        left join fetch i.event e
        where c.userId = :userId
        """)
    Optional<Cart> findWithItemsByUserId(@Param("userId") UUID userId);

    Optional<Cart> findByUserId(UUID userId);
}
