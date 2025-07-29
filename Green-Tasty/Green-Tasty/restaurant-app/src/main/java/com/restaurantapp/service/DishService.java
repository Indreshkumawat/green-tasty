package com.restaurantapp.service;

import com.restaurantapp.dto.Dish;
import com.restaurantapp.exception.DishNotFoundException;
import com.restaurantapp.exception.ValidationException;
import com.restaurantapp.repo.DishRepo;
import com.restaurantapp.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DishService {
    private final DishRepo dishRepo;


    private static final Set<String> ALLOWED_DISH_TYPES = Set.of("DESSERT", "APPETIZER", "MAIN_COURSE");
    private static final Set<String> ALLOWED_SORT = Set.of("popularity,asc", "popularity,desc", "price,asc", "price,desc");

    @Autowired
    public DishService(DishRepo dishRepo) {
        this.dishRepo = dishRepo;
    }

    public List<Map<String, Object>> getPopularDishes() throws DishNotFoundException {
            return dishRepo.getPopularDishes();
    }

    public List<Map<String, Object>> getAllDishes(String dishType, String sort) throws Exception {
        // Validate dishType
        if (dishType != null && !dishType.isBlank() && !ALLOWED_DISH_TYPES.contains(dishType.toUpperCase())) {
            throw new IllegalArgumentException("Invalid dishType provided. Allowed values are: DESSERT, APPETIZER, MAIN_COURSE.");
        }

        // Validate sort parameter
        if (sort != null && !ALLOWED_SORT.contains(sort.toLowerCase())) {
            throw new IllegalArgumentException("Invalid sort parameter. Allowed values are: popularity,asc, popularity,desc, price,asc, price,desc.");
        }

        // Call the repository to fetch dishes
        return dishRepo.getDishes(dishType, sort);
    }

    public Dish getDishById(String id) throws Exception {
        return dishRepo.getDishById(id);
    }

    public List<Map<String, Object>> getAvailableDishes(String reservationId, String dishType, String sort) throws Exception {

        String validateReservationId = ValidationUtil.isValidString(reservationId, "RESERVATION-ID");
        if(validateReservationId != null)
            throw new ValidationException(validateReservationId);

        // Validate dish type
        if (dishType != null && !dishType.isBlank() && !ALLOWED_DISH_TYPES.contains(dishType.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid dish type. Allowed values are: DESSERT, APPETIZER, MAIN_COURSE");
        }

        // Validate sort parameter
        if (sort != null && !ALLOWED_SORT.contains(sort.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid sort parameter. Allowed values are: popularity,asc, popularity,desc, price,asc, price,desc");
        }

        // Call repository layer and let it propagate known exceptions
        return dishRepo.getAvailableDishes(reservationId, dishType, sort);
    }
}
