package com.reportsapp.controller;

import com.reportsapp.feign_client.AuthFeignClient;
import com.reportsapp.service.ReportsSenderService;
import com.reportsapp.service.TokenService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScheduledReportSender {
    private final ReportsSenderService reportsSenderService;
    private final AuthFeignClient authFeignClient;
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(ScheduledReportSender.class);
    private volatile boolean shuttingDown = false;


    @Autowired
    public ScheduledReportSender(ReportsSenderService reportsSenderService, AuthFeignClient authFeignClient, TokenService tokenService) {
        this.reportsSenderService = reportsSenderService;
        this.authFeignClient = authFeignClient;
        this.tokenService = tokenService;
    }

    @Scheduled(cron = "0 0 0 */7 * ?")
    public void generateAndSendReports() {
        if (shuttingDown) {
            logger.info("Skipping task execution as the application is shutting down...");
            return;
        }
        authenticate();
        reportsSenderService.sendReports();
    }

    private void authenticate() {
        // Replace with dynamic credentials if needed
        Map<String, String> credentials = Map.of(
                "email", "krishnendu29@example.com",
                "password", "Krish@123"
        );

        // Call Restaurant App's /auth/sign-in API
        Map<String, Object> authResponse = authFeignClient.authenticate(credentials);

        // Extract token from the response
        String token = (String) authResponse.get("accessToken");
        if (token != null) {
            tokenService.setToken(token);
        } else {
            logger.error("Failed to authenticate during report generation.");
        }
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("Application is shutting down...");
        shuttingDown = true; // Mark shutting down flag
    }
}
