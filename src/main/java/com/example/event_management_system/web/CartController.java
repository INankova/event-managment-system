package com.example.event_management_system.web;

import com.example.event_management_system.cart.model.Cart;
import com.example.event_management_system.cart.service.CartService;
import com.example.event_management_system.cart.service.CheckoutService;
import com.example.event_management_system.security.AuthenticationMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CheckoutService checkoutService;

    @Autowired
    public CartController(CartService cartService, CheckoutService checkoutService) {
        this.cartService = cartService;
        this.checkoutService = checkoutService;
    }

    @GetMapping
    public ModelAndView shoppingCart(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        Cart cart = cartService.getShoppingCart(authenticationMetaData.getId());

        BigDecimal totalPrice = cartService.calculateTotalPrice(cart);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("shopping-cart");
        modelAndView.addObject("cart", cart);
        modelAndView.addObject("totalPrice", totalPrice);
        modelAndView.addObject("user", authenticationMetaData);

        return modelAndView;
    }

    @PostMapping("/add/{eventId}")
    public String addToCart(@PathVariable UUID eventId, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        cartService.addToCart(authenticationMetaData.getId(), eventId);

        return "redirect:/home";
    }

    @PostMapping("/increase-quantity/{eventId}")
    public String increaseQuantity(@PathVariable UUID eventId, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        cartService.increaseQuantity(authenticationMetaData.getId(), eventId);

        return "redirect:/cart";
    }

    @PostMapping("/decrease-quantity/{eventId}")
    public String decreaseQuantity(@PathVariable UUID eventId, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        cartService.decreaseQuantity(authenticationMetaData.getId(), eventId);
        return "redirect:/cart";
    }

    @PostMapping("/remove-item/{eventId}")
    public String removeItem(@PathVariable UUID eventId, @AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {

        cartService.removeItem(authenticationMetaData.getId(), eventId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(@AuthenticationPrincipal AuthenticationMetaData authenticationMetaData) {
        checkoutService.processCheckout(authenticationMetaData.getId());
        return "redirect:/users/profile";
    }
}
