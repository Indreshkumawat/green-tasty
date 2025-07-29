package com.reportsapp.controller;

import com.reportsapp.service.ReportsSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/send-report")
public class ReportsController {

    private final ScheduledReportSender scheduledReportSender;

    @Autowired
    public ReportsController(ScheduledReportSender scheduledReportSender) {
         this.scheduledReportSender = scheduledReportSender;

    }

    @GetMapping
    public ResponseEntity<Map<String, String>> triggerSendReports() {
        scheduledReportSender.generateAndSendReports();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Report generated and email sent successfully.");
        return ResponseEntity.ok(response);
    }

}
