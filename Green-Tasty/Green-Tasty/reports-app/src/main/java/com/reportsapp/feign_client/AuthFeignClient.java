package com.reportsapp.feign_client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "auth-client", url = "${restaurant.base.url}")
public interface AuthFeignClient {

    // Restaurant App's /auth/sign-in API
    @PostMapping("/auth/sign-in")
    Map<String, Object> authenticate(@RequestBody Map<String, String> credentials);
}
