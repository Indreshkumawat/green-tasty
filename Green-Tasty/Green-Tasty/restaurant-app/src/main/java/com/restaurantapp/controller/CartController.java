package com.restaurantapp.controller;

import com.restaurantapp.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "/cart")
public class CartController {
    private CartService cartService;
    private final Logger logger;

    @Autowired
    public CartController(CartService cartService)
    {
        this.cartService = cartService;
        logger = LoggerFactory.getLogger(CartController.class);
    }

    @GetMapping
    public ResponseEntity<Object> getCart(Authentication authentication) throws Exception
    {
        logger.info("GET/cart called.....");
        String customerEmail = authentication.getName();
        return ResponseEntity.ok(cartService.getCart(customerEmail));
    }

    @PutMapping
    public ResponseEntity<Object> putCart(@RequestBody Map<String,Object> requestBody, Authentication authentication) throws Exception
    {
        logger.info("PUT/cart called.......");
        String customerEmail = authentication.getName();
        return ResponseEntity.ok(cartService.putCart(requestBody, customerEmail));
    }
}
