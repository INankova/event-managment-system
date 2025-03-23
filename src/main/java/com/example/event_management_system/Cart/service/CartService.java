package com.example.event_management_system.Cart.service;

import com.example.event_management_system.Cart.model.Cart;
import com.example.event_management_system.Cart.model.CartItem;
import com.example.event_management_system.Cart.repository.CartRepository;
import com.example.event_management_system.Event.model.Event;
import com.example.event_management_system.Event.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CartService(CartRepository cartRepository, EventRepository eventRepository) {
        this.cartRepository = cartRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @CacheEvict(value = "cart", key = "#userId")
    public void addToCart(UUID userId, UUID eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Събитието не е намерено!"));


        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getEvent().getId().equals(eventId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            cart.getItems().add(new CartItem(event, 1));
        }
        cartRepository.save(cart);


    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void increaseQuantity(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        item.setQuantity(item.getQuantity() + 1);
        cartRepository.save(cart);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cart", key = "#userId"),
            @CacheEvict(value = "cartTotal", key = "#userId")
    })
    public void decreaseQuantity(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cart.getItems().remove(item); // Ако количеството е 1, премахваме артикула
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItem(UUID userId, UUID eventId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Количката не е намерена!"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getEvent().getId().equals(eventId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Артикулът не е намерен!"));

        cart.getItems().remove(item);
        cartRepository.save(cart);
    }

    private Cart createNewCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        return cart;
    }

    @Cacheable(value = "cart", key = "#userId")
    public Cart getShoppingCart(UUID userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Cacheable(value = "cartTotal", key = "#cart.userId")
    public BigDecimal calculateTotalPrice(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return cart.getItems().stream()
                .map(item -> item.getEvent().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
