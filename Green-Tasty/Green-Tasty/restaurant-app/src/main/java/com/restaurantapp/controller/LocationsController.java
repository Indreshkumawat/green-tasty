package com.restaurantapp.controller;

import com.restaurantapp.dto.Location;
import com.restaurantapp.exception.LocationNotFoundException;
import com.restaurantapp.service.LocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/locations")
public class LocationsController{
    public LocationService locationService;

    public LocationsController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping()
    public ResponseEntity<Object> getAllLocationsController(){
        List<Location> locations = locationService.getAllLocationsService();
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    @GetMapping("/{id}/speciality-dishes")
    public ResponseEntity<Object> getSpecialityDishesByLocationController(@PathVariable String id) throws Exception {
        List<Map<String,Object>> specialDishes = locationService.getSpecialityDishesByLocationService(id);
        return new ResponseEntity<>(specialDishes,HttpStatus.OK);
    }

    @GetMapping("/{id}/feedbacks")
    public ResponseEntity<Object> getFeedbacksByLocationController(@PathVariable(value ="id") String id,
                                                                   @RequestParam(required = false) Map<String, String> allParams) throws Exception {

        Map<String, Object> feedbacksByLocation = locationService.getFeedbacksByLocationService(id,allParams);
        return new ResponseEntity<>(feedbacksByLocation,HttpStatus.OK);
    }

    @GetMapping("/select-options")
    public ResponseEntity<Object> getLocationInfoOptionsController(){
        List<Map<String,Object>> locationInfoOptions = locationService.getLocationInfoOptionsService();
        return new ResponseEntity<>(locationInfoOptions,HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> getLocationByIdController(@PathVariable(value ="id") String id) throws LocationNotFoundException {
        Location location = locationService.getLocationByIdService(id);
        return new ResponseEntity<>(location,HttpStatus.OK);
    }
}
