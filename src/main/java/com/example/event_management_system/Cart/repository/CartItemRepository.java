package com.example.event_management_system.Cart.repository;

import com.example.event_management_system.Cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    @Query("""
        SELECT COALESCE(SUM(ci.quantity), 0)
        FROM CartItem ci
        WHERE ci.cart.id = :cartId
    """)
    int getTotalQuantityByCartId(UUID cartId);
}
