package com.restaurantapp.service;

import com.restaurantapp.dto.*;
import com.restaurantapp.exception.*;
import com.restaurantapp.repo.BookingRepo;
import com.restaurantapp.repo.DishRepo;
import com.restaurantapp.repo.LocationRepo;
import com.restaurantapp.util.ValidationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class CartService {
    private BookingRepo bookingRepo;
    private DishRepo dishRepo;
    private LocationRepo locationRepo;
    private final Logger logger;

    public CartService(BookingRepo bookingRepo, DishRepo dishRepo, LocationRepo locationRepo)
    {
        this.bookingRepo = bookingRepo;
        this.dishRepo = dishRepo;
        this.locationRepo = locationRepo;
        logger = LoggerFactory.getLogger(CartService.class);
    }

    public Map<Object,Object> buildCartResponse(String customerEmail) throws Exception
    {
        Map<Object, Object> responseBody = new HashMap<>();
        List<Map<String, Object>> content = bookingRepo.getAllReservationWithReservedStatus(customerEmail);

        for (Map<String, Object> entry : content) {
            Map<String, String> preOrder = (Map<String, String>) entry.get("preOrder");

            entry.remove("preOrder");

            List<Map<String, String>> dishItems = new ArrayList<>();

            for (Map.Entry<String, String> preOrderEntry : preOrder.entrySet()) {
                Map<String, String> dishDetails = new HashMap<>();
                String dishId = preOrderEntry.getKey();
                String quantity = preOrderEntry.getValue();

                Dish dish = dishRepo.getDishById(dishId);
                dishDetails.put("dishId", dish.getId());
                dishDetails.put("dishImageUrl", dish.getImageUrl());
                dishDetails.put("dishName", dish.getName());
                dishDetails.put("dishPrice", dish.getPrice());
                dishDetails.put("dishQuantity", quantity);
                dishItems.add(dishDetails);
            }

            entry.put("dishItems", dishItems);
        }

        responseBody.put("content", content);

        return responseBody;
    }

    public Map<Object,Object> getCart(String customerEmail) throws Exception
    {

        logger.info("Going to build the cart response.....");

        return buildCartResponse(customerEmail);


    }

    public Map<Object,Object> putCart(Map<String,Object> requestBody, String customerEmail) throws Exception
    {
        Map<Object,Object> responseBody;
        String reservationId = (String) requestBody.getOrDefault("reservationId", "");
        String validateReservationId = ValidationUtil.isValidString(reservationId, "Reservation-ID");
        if (validateReservationId != null) {
            throw new ValidationException(validateReservationId);
        }
        String preOrderState = (String) requestBody.getOrDefault("state", "");
        Reservation reservation;

        if (bookingRepo.isReserved(reservationId))
            reservation = bookingRepo.getReservationById(reservationId);
        else
            throw new ValidationException("Pre-order is only allowed when reservation is in RESERVED state.");

        Location locationDetails = locationRepo.getLocationById(reservation.getLocationId());

        String validatePreOrderState = ValidationUtil.validatePreOrderStateValue(preOrderState);
        if (validatePreOrderState != null) {
            throw new ValidationException(validatePreOrderState);
        }

        List<Map<String, Object>> dishItems = (List<Map<String, Object>>) requestBody.getOrDefault("dishItems", null);
        if (dishItems == null) {
            String msg = "Invalid request payload. Missing dishItems!!!";
            throw new ValidationException(msg);
        }
        Map<String, String> dishQuantityMap = new HashMap<>();

        for (Map<String, Object> dish : dishItems) {
            long quantity = Long.parseLong(dish.getOrDefault("dishQuantity", 0).toString());
            if (quantity <= 0)
                continue;
            String dishId = (String) dish.getOrDefault("dishId", "");

            String validateDishId = ValidationUtil.isValidString(dishId, "Dish ID");
            String validateDishQty = ValidationUtil.isValidQuantity(quantity, reservation.getGuestsNumber());
            if (validateDishId != null) {
                throw new ValidationException(validateDishId);
            }
            if (validateDishQty != null) {
                throw new PreOrderDishQuantityLimitException(validateDishQty);
            }


            if (dishRepo.checkIfDishIdExists(dishId)) {
                long qty = Long.parseLong(dishQuantityMap.getOrDefault(dishId, "0"));
                dishQuantityMap.put(dishId, ("" + (qty + quantity)));
            }
        }

        bookingRepo.updateReservationWithPreOrderDetails(reservationId, preOrderState, dishQuantityMap, LocalDateTime.now(ZoneId.of(locationDetails.getZone())));

        return buildCartResponse(customerEmail);

    }
}
