package com.restaurantapp.service;

import com.restaurantapp.dto.LocationReport;
import com.restaurantapp.dto.WaiterReport;
import com.restaurantapp.repo.ReportsRepo;
import com.restaurantapp.util.DateFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportsService {
    ReportsRepo reportsRepo;

    @Autowired
    public ReportsService(ReportsRepo reportsRepo) {
        this.reportsRepo = reportsRepo;
    }

    public Object getReports(HashMap<Object, Object> queryParameters) throws IOException {
        ArrayList<Map<String, Object>> reportArray = new ArrayList<>();
        String reportType = (String) queryParameters.get("reportType");
        String fromDateString = (String) queryParameters.get("fromDate");
        LocalDate fromDate = LocalDate.parse(fromDateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String toDateString = (String) queryParameters.get("toDate");
        LocalDate toDate = LocalDate.parse(toDateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        long diff = ChronoUnit.DAYS.between(fromDate, toDate);
        String csvLink = "";
        String pdfLink = "";


        if (reportType == null || reportType.isBlank() || (!reportType.equalsIgnoreCase("Staff_Performance") && !reportType.equalsIgnoreCase("Location_Performance"))) {
            throw new IllegalArgumentException("Please provide a valid reportType.");
        } else if (reportType.equalsIgnoreCase("Staff_Performance")) {
            Map<String, WaiterReport> reportDataWaiterPrev = reportsRepo.fetchWaiterReports(
                    DateFormatter.convertToStandardFormat(fromDate.minusDays(diff).toString()),
                    fromDateString,
                    (String) queryParameters.get("waiterId"),
                    (String) queryParameters.get("locationId"));
            Map<String, WaiterReport> reportDataWaiterCurr = reportsRepo.fetchWaiterReports(
                    fromDateString,
                    toDateString,
                    (String) queryParameters.get("waiterId"),
                    (String) queryParameters.get("locationId"));

            csvLink = reportsRepo.uploadToS3("run8-team9-deployment-bucket", "reports/WaiterReport" + fromDateString + "-" + toDateString + ".csv", "text/csv", reportsRepo.generateWaiterCSV(reportDataWaiterCurr, reportDataWaiterPrev).getBytes());
            pdfLink = reportsRepo.uploadToS3("run8-team9-deployment-bucket", "reports/WaiterReport" + fromDateString + "-" + toDateString + ".pdf", "application/pdf", reportsRepo.generateWaiterPDF(reportDataWaiterCurr, reportDataWaiterPrev, fromDateString, toDateString));

            for (WaiterReport report : reportDataWaiterCurr.values()) {
                WaiterReport prevReport = reportDataWaiterPrev.getOrDefault(report.getWaiterEmail(), WaiterReport.getBlankReport());
                double deltaOrdersProcessed = prevReport.getOrdersProcessed() == 0
                        ? 0
                        : ((report.getOrdersProcessed() - prevReport.getOrdersProcessed()) / prevReport.getOrdersProcessed()) * 100;

                double avgServiceFeedbackCurr = report.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double avgServiceFeedbackPrev = prevReport.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double deltaAvgServiceFeedback = avgServiceFeedbackPrev == 0
                        ? 0
                        : ((avgServiceFeedbackCurr - avgServiceFeedbackPrev) / avgServiceFeedbackPrev) * 100;

                reportArray.add(new LinkedHashMap<>() {
                    {
                        put("locationId", report.getLocationId());
                        put("waiterName", report.getWaiterName());
                        put("startDate", report.getStartDate());
                        put("endDate", report.getEndDate());
                        put("workingHours", report.getHoursWorked());
                        put("ordersProcessed", report.getOrdersProcessed());
                        put("deltaOrdersProcessed", deltaOrdersProcessed);
                        put("avgServiceFeedback", avgServiceFeedbackCurr);
                        put("minServiceFeedback", report.getMinServiceFeedback());
                        put("deltaServiceFeedback", deltaAvgServiceFeedback);
                    }
                });
            }
        } else {
            Map<String, WaiterReport> reportDataWaiterCurr = reportsRepo.fetchWaiterReports(
                    fromDateString,
                    toDateString,
                    (String) queryParameters.get("waiterId"),
                    (String) queryParameters.get("locationId")
            );
            Map<String, WaiterReport> reportDataWaiterPrev = reportsRepo.fetchWaiterReports(
                    DateFormatter.convertToStandardFormat(fromDate.minusDays(diff).toString()),
                    fromDateString,
                    (String) queryParameters.get("waiterId"),
                    (String) queryParameters.get("locationId"));
            Map<String, LocationReport> reportDataLocationCurr = reportsRepo.generateLocationReports(reportDataWaiterCurr);
            Map<String, LocationReport> reportDataLocationPrev = reportsRepo.generateLocationReports(reportDataWaiterPrev);

            csvLink = reportsRepo.uploadToS3("run8-team9-deployment-bucket", "reports/LocationReport" + fromDateString + "-" + toDateString + ".csv", "text/csv", reportsRepo.generateLocationCSV(reportDataLocationCurr, reportDataLocationPrev).getBytes());
            pdfLink = reportsRepo.uploadToS3("run8-team9-deployment-bucket", "reports/LocationReport" + fromDateString + "-" + toDateString + ".pdf", "application/pdf", reportsRepo.generateLocationPDF(reportDataLocationCurr, reportDataLocationPrev, fromDateString, toDateString));

            for (LocationReport report : reportDataLocationCurr.values()) {
                LocationReport prevReport = reportDataLocationPrev.getOrDefault(report.getLocationId(), LocationReport.getBlankReport());

                double deltaOrdersProcessed = prevReport.getOrdersProcessed() == 0
                        ? 0
                        : ((report.getOrdersProcessed() - prevReport.getOrdersProcessed()) / prevReport.getOrdersProcessed()) * 100;

                double avgCuisineFeedbackCurr = report.getTotalCuisineFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double avgCuisineFeedbackPrev = prevReport.getTotalCuisineFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double deltaAvgCuisineFeedback = avgCuisineFeedbackPrev == 0
                        ? 0
                        : ((avgCuisineFeedbackCurr - avgCuisineFeedbackPrev) / avgCuisineFeedbackPrev) * 100;

                double deltaRevenue = prevReport.getRevenue() == 0
                        ? 0
                        : ((report.getRevenue() - prevReport.getRevenue()) / prevReport.getRevenue()) * 100;

                reportArray.add(new LinkedHashMap<>() {
                    {
                        put("locationId", report.getLocationId());
                        put("startDate", report.getStartDate());
                        put("endDate", report.getEndDate());
                        put("ordersProcessed", report.getOrdersProcessed());
                        put("deltaOrdersProcessed", deltaOrdersProcessed);
                        put("avgCuisineFeedback", avgCuisineFeedbackCurr);
                        put("minCuisineFeedback", report.getMinCuisineFeedback());
                        put("deltaCuisineFeedback", deltaAvgCuisineFeedback);
                        put("revenue", report.getRevenue());
                        put("deltaRevenue", deltaRevenue);
                    }
                });
            }
        }
        return Map.of("content", reportArray,
                "downloadLinkCSV", csvLink,
                "downloadLinkPDF", pdfLink
        );
    }
}
