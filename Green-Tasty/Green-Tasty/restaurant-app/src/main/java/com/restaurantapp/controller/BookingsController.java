package com.restaurantapp.controller;

import com.restaurantapp.service.BookingService;
import com.restaurantapp.service.TableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingsController {

    private BookingService bookingService;
    private TableService tableService;

    @Autowired
    public BookingsController(BookingService bookingService, TableService tableService) {
        this.bookingService = bookingService;
        this.tableService = tableService;
    }

    @GetMapping(value = "/tables")
    public ResponseEntity<Object> getAvailableTables(@RequestParam(required = false, value = "locationId") String locationId,
                                                     @RequestParam(required = false, value = "date") String date,
                                                     @RequestParam(required = false, value = "time") String time,
                                                     @RequestParam(required = false, value = "guests") String guests) throws Exception {
        return ResponseEntity.ok(tableService.getAvailableTables(new HashMap<>() {{
            put("locationId", locationId);
            put("date", date);
            put("time", time);
            put("guests", guests);
        }}));
    }

    @PostMapping(value = "/client")
    public ResponseEntity<Object> postClientBooking(@RequestBody String requestBody, Authentication authentication) throws Exception {
        return ResponseEntity.ok(bookingService.postClientBooking(requestBody,authentication.getName()));
    }

    @PostMapping(value = "/waiter")
    public ResponseEntity<Object> postWaiterBooking(@RequestBody String requestBody, Authentication authentication) throws Exception {
        return ResponseEntity.ok(bookingService.postWaiterBooking(requestBody,authentication.getName()));
    }
}
