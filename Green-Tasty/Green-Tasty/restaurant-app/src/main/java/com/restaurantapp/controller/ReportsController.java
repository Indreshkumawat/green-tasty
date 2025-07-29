package com.restaurantapp.controller;

import com.restaurantapp.service.ReportsService;
import com.restaurantapp.util.DateFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;

@RestController
@RequestMapping("/reports")
public class ReportsController {
    private ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping
    public ResponseEntity<Object> getReports(@RequestParam(required = false, value = "reportType") String reportType,
                                             @RequestParam(required = false, value = "fromDate") String fromDate,
                                             @RequestParam(required = false, value = "toDate") String toDate,
                                             @RequestParam(required = false, value = "locationId") String locationId,
                                             @RequestParam(required = false, value = "waiterId") String waiterId) throws Exception {
        return ResponseEntity.ok(reportsService.getReports(new HashMap<>() {{
            put("reportType", reportType);
            put("fromDate", fromDate == null ? DateFormatter.convertToStandardFormat(LocalDate.now().minusDays(7).toString()) : DateFormatter.convertToStandardFormat(fromDate));
            put("toDate", toDate == null ? DateFormatter.convertToStandardFormat(LocalDate.now().toString()) : DateFormatter.convertToStandardFormat(toDate));
            put("locationId", locationId);
            put("waiterId", waiterId);
        }}));
    }
}
