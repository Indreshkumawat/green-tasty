package com.restaurantapp.controller;

import com.restaurantapp.dto.Dish;
import com.restaurantapp.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dishes")
public class DishController {
    DishService dishService;

    @Autowired
    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping
    public ResponseEntity<Object> getDishes(
            @RequestParam(required = false) String dishType,
            @RequestParam(required = false) String sort
    ){
        try{
           List<Map<String, Object>> dishes = dishService.getAllDishes(dishType, sort);
           return ResponseEntity.ok(Map.of("content",dishes));
        }
        catch (IllegalArgumentException e) {
            // Handle validation errors for dishType and sort
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Handle other unexpected errors
            return ResponseEntity.internalServerError().body(Map.of("error", "An error occurred", "details", e.getMessage()));
        }
    }
    @GetMapping("/popular")
    public ResponseEntity<List<Map<String, Object>>> getAllPopularDishes() {
        List<Map<String, Object>> dishes = dishService.getPopularDishes();
        return new ResponseEntity<>(dishes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable String id) throws Exception {

        return ResponseEntity.ok(dishService.getDishById(id));
    }


}
