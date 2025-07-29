package com.restaurantapp.controller;

import com.restaurantapp.dto.SignIn;
import com.restaurantapp.dto.SignUp;
import com.restaurantapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Object> userSignUp(@RequestBody SignUp signUp) throws Exception {
        return ResponseEntity.ok(userService.userSignUp(signUp));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Object> userSignIn(@RequestBody SignIn signIn) throws Exception {
        return ResponseEntity.ok(userService.userSignIn(signIn));
    }
}
