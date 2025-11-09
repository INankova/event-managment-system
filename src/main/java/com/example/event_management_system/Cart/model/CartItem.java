package com.example.event_management_system.Cart.model;

import com.example.event_management_system.Event.model.Event;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private Event event;

    private Integer quantity;

    private BigDecimal price;

    @ManyToOne
    private Cart cart;

}
