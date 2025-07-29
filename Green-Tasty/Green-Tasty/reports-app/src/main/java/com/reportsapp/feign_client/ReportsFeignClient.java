package com.reportsapp.feign_client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "restaurant-app", url = "${restaurant.base.url}" ,configuration = FeignClientConfig.class) // Use the restaurant app's base URL
public interface ReportsFeignClient {

    @GetMapping("/reports")
    Map<String, Object> getReports(@RequestParam("reportType") String reportType,
                                   @RequestParam(value = "fromDate", required = false) String fromDate,
                                   @RequestParam(value = "toDate", required = false) String toDate,
                                   @RequestParam(value = "locationId", required = false) String locationId,
                                   @RequestParam(value = "waiterId", required = false) String waiterId);
}