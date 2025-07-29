package com.restaurantapp.controller;

import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.dto.UserProfileUpdateRequest;
import com.restaurantapp.exception.HttpMessageNotReadableException;
import com.restaurantapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UsersController {

    private UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value="/profile")
    public ResponseEntity<Object> getUsersProfile(Authentication authentication) throws Exception {
        return ResponseEntity.ok(userService.getUsersProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request, Authentication authentication) throws Exception {
        if(authentication==null || !authentication.isAuthenticated()){
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        userService.updateProfile(email, request); // Delegate business logic to service layer

        return ResponseEntity.ok(Map.of("message", "Profile has been successfully updated."));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request, Authentication authentication) throws Exception {

        if(request==null){
            throw new HttpMessageNotReadableException("Missing request body");
        }

        if(authentication==null || !authentication.isAuthenticated()){
            throw new AccessDeniedException("Authentication required");
        }

        String email = authentication.getName();
        userService.updateProfilePassword(email,request);
        return ResponseEntity.ok(Map.of("message", "Password has been successfully updated"));
    }

    @GetMapping("/customer-info")
    public ResponseEntity<Object> getCustomerInfo() throws Exception {
        return ResponseEntity.ok(userService.getAllCustomers());
    }
}
