package com.example.event_management_system.Cart.service;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.Cart.model.CartItem;
import com.example.event_management_system.Cart.repository.CartRepository;
import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final EventRepository eventRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void addToCart(UUID userId, UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Събитието не е намерено!"));

        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getEvent().getId().equals(eventId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem it = existingItem.get();
            it.setQuantity((it.getQuantity() == null ? 0 : it.getQuantity()) + 1);
        } else {
            CartItem newItem = CartItem.builder()
                    .event(event)
                    .quantity(1)
                    .price(event.getPrice())
                    .cart(cart)
                    .build();
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void increaseQuantity(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        item.setQuantity((item.getQuantity() == null ? 0 : item.getQuantity()) + 1);
        cartRepository.save(cart);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void decreaseQuantity(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        int q = item.getQuantity() == null ? 1 : item.getQuantity();
        if (q > 1) {
            item.setQuantity(q - 1);
        } else {
            cart.getItems().remove(item);
            item.setCart(null);
        }

        cartRepository.save(cart);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void removeItem(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        cart.getItems().remove(item);
        item.setCart(null);
        cartRepository.save(cart);
    }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart getShoppingCart(UUID userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Cacheable(value = "cartTotal", key = "#cart.userId")
    public BigDecimal calculateTotalPrice(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cart.getItems().stream()
                .map(item -> {
                    BigDecimal unit = item.getPrice() != null ? item.getPrice() : item.getEvent().getPrice();
                    int qty = item.getQuantity() == null ? 1 : item.getQuantity();
                    return unit.multiply(BigDecimal.valueOf(qty));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

