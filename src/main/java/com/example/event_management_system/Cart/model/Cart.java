package com.example.event_management_system.Cart.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private BigDecimal totalPrice;

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

}
