package com.reportsapp.service;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private String token; // Stores the current token dynamically

    public void setToken(String token) {
        this.token = token; // Store the token
    }

    public String getToken() {
        return token; // Retrieve the token
    }

}