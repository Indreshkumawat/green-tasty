package com.restaurantapp.controller;

import com.restaurantapp.service.BookingService;
import com.restaurantapp.service.DishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/reservations")
public class ReservationController {
    private DishService dishService;
    private BookingService bookingService;
    private final Logger logger;

    @Autowired
    public ReservationController(BookingService bookingService, DishService dishService) {
        this.bookingService = bookingService;
        this.dishService = dishService;
        this.logger = LoggerFactory.getLogger(ReservationController.class);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Object> editReservation(@RequestBody Map<String,String> requestBody, @PathVariable String id) throws Exception
    {
        logger.info("PATCH/reservations/{} called", id);
        return ResponseEntity.ok(bookingService.editReservation(requestBody, id));
    }
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Object> deleteReservation(@PathVariable String id) throws Exception
    {
        logger.info("DELETE/reservations/{} called", id);
        return ResponseEntity.ok(bookingService.deleteReservation(id));

    }

    @GetMapping("/{id}/available-dishes")
    public ResponseEntity<Object> getAvailableDishes(
            @PathVariable String id,
            @RequestParam(required = false) String dishType,
            @RequestParam(required = false) String sort
    ) throws Exception {
        var dishes = dishService.getAvailableDishes(id, dishType, sort);
        return ResponseEntity.ok(Map.of("content", dishes));
    }

    @PostMapping("/{id}/order/{dishId}")
    public ResponseEntity<Object> addDishToReservationForCart(
            @PathVariable String id,
            @PathVariable String dishId
    ) throws Exception {
        bookingService.addDishToCartForReservation(id,dishId);
        return ResponseEntity.ok(Map.of("message", "Dish has been added to the cart."));
    }

    @GetMapping
    public ResponseEntity<Object> getAllReservations(Authentication authentication,
                                                     @RequestParam(required = false, value = "date") String date,
                                                     @RequestParam(required = false, value = "time") String time,
                                                     @RequestParam(required = false, value = "tableNumber") String tableNumber) throws Exception {
        return ResponseEntity.ok(bookingService.getAllReservations(authentication.getName(),date,time,tableNumber));
    }
}

