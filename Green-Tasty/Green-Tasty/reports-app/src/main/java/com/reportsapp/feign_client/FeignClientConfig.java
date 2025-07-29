package com.reportsapp.feign_client;

import com.reportsapp.service.TokenService;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignClientConfig {
    private final TokenService tokenService;

    public FeignClientConfig(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String token = tokenService.getToken(); // Retrieve the latest token
            if (token != null) {
                requestTemplate.header("Authorization", "Bearer " + token); // Add Bearer token to header
            }
        };
    }
}
