package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.restaurantapp.dto.LocationReport;
import com.restaurantapp.dto.WaiterReport;
import com.restaurantapp.util.DateFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsRepo {
    private final AmazonDynamoDB amazonDynamoDBClient;
    private final DynamoDB dynamoDBClient;
    private final AmazonS3 s3Client;
    private final Table waiterTable;
    private final Table reportsTable;

    @Value("${reports.table}") String reportTableName;

    public ReportsRepo(AmazonDynamoDB amazonDynamoDBClient, AmazonS3 amazonS3Client,String waiterTableName,String reportTableName) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDBClient = new DynamoDB(amazonDynamoDBClient);
        this.s3Client = amazonS3Client;
        this.waiterTable = dynamoDBClient.getTable(waiterTableName);
        this.reportsTable = dynamoDBClient.getTable(reportTableName);
    }

    public Map<String, WaiterReport> fetchWaiterReports(String startDate, String endDate, String waiterId, String locationId) {
        String filterExpression = "#date BETWEEN :startDate AND :endDate";
        Map<String, String> attributeNameMap = new HashMap<>(Map.of("#date", "date"));
        Map<String, AttributeValue> attributeValueMap = new HashMap<>(Map.of(
                ":startDate", new AttributeValue().withS(DateFormatter.convertToISOFormat(startDate)),
                ":endDate", new AttributeValue().withS(DateFormatter.convertToISOFormat(endDate))
        ));

        if (waiterId != null && locationId != null && !waiterId.isBlank() && !locationId.isBlank()) {
            filterExpression += " AND #waiter_email = :waiterId AND #location_id = :locationId";
            attributeNameMap.putAll(Map.of(
                    "#waiter_email", "waiter_email",
                    "#location_id", "location_id"
            ));
            attributeValueMap.putAll(Map.of(
                    ":waiterId", new AttributeValue().withS(waiterId),
                    ":locationId", new AttributeValue().withS(locationId)
            ));

        } else if ((waiterId == null || waiterId.isBlank()) && locationId != null) {
            filterExpression += " AND #location_id = :locationId";
            attributeNameMap.put("#location_id", "location_id");
            attributeValueMap.put(":locationId", new AttributeValue().withS(locationId));
        } else if (waiterId != null && !waiterId.isBlank()) {
            filterExpression += " AND #waiter_email = :waiterId";
            attributeNameMap.put("#waiter_email", "waiter_email");
            attributeValueMap.put(":waiterId", new AttributeValue().withS(waiterId));
        }

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(reportTableName)
                .withFilterExpression(filterExpression)
                .withExpressionAttributeNames(attributeNameMap)
                .withExpressionAttributeValues(attributeValueMap);

        ScanResult scanResult = amazonDynamoDBClient.scan(scanRequest);
        Map<String, WaiterReport> waiterReportsMap = new HashMap<>();

        scanResult.getItems().forEach(item -> {
            String waiterEmail = item.get("waiter_email").getS();
            double hoursWorked = Double.parseDouble(item.get("waiter_working_hours").getN());
            double ordersProcessed = Double.parseDouble(item.get("waiter_orders_processed").getN());
            List<Double> totalServiceFeedback = item.get("total_service_feedback").getL()
                    .stream().mapToDouble(x -> Double.parseDouble(x.getN())).boxed().collect(Collectors.toList());
            double minServiceFeedback = Double.parseDouble(item.get("minimum_service_feedback").getN());
            List<Double> totalCuisineFeedback = item.get("total_cuisine_feedback").getL()
                    .stream().mapToDouble(x -> Double.parseDouble(x.getN())).boxed().collect(Collectors.toList());
            double minCuisineFeedback = Double.parseDouble(item.get("minimum_cuisine_feedback").getN());
            double revenue = Double.parseDouble(item.get("total_revenue").getN());

            if (!waiterReportsMap.containsKey(waiterEmail)) {
                Item waiterDetails = waiterTable.getItem("email", waiterEmail);
                String location = item.get("location_id").getS();
                String waiterName = waiterDetails.getString("first_name") + " " + waiterDetails.getString("last_name");
                waiterReportsMap.put(waiterEmail, new WaiterReport(location, waiterName, waiterEmail, startDate, endDate, hoursWorked, ordersProcessed, totalServiceFeedback, minServiceFeedback, totalCuisineFeedback, minCuisineFeedback, revenue));
            } else {
                WaiterReport existingReport = waiterReportsMap.get(waiterEmail);
                existingReport.setHoursWorked(existingReport.getHoursWorked() + hoursWorked);
                existingReport.setOrdersProcessed(existingReport.getOrdersProcessed() + ordersProcessed);
                existingReport.getTotalServiceFeedback().addAll(totalServiceFeedback);
                existingReport.setMinServiceFeedback(existingReport.getMinServiceFeedback() + minServiceFeedback);
                existingReport.getTotalCuisineFeedback().addAll(totalCuisineFeedback);
                existingReport.setMinCuisineFeedback(existingReport.getMinCuisineFeedback() + minCuisineFeedback);
                existingReport.setRevenue(existingReport.getRevenue() + revenue);
            }
        });
        return waiterReportsMap;
    }

    public Map<String, LocationReport> generateLocationReports(Map<String, WaiterReport> waiterReportMap) {
        Map<String, LocationReport> locationReportsMap = new HashMap<>();

        for (WaiterReport waiterReport : waiterReportMap.values()) {
            String locationId = waiterReport.getLocationId();

            if (!locationReportsMap.containsKey(locationId)) {
                locationReportsMap.put(locationId, new LocationReport(
                        locationId,
                        waiterReport.getStartDate(),
                        waiterReport.getEndDate(),
                        waiterReport.getOrdersProcessed(),
                        new ArrayList<>() {{
                            addAll(waiterReport.getTotalCuisineFeedback());
                        }},
                        waiterReport.getMinCuisineFeedback(),
                        waiterReport.getRevenue()
                ));
            }

            LocationReport existingLocationReport = locationReportsMap.get(locationId);
            existingLocationReport.setOrdersProcessed(existingLocationReport.getOrdersProcessed() + waiterReport.getOrdersProcessed());
            existingLocationReport.getTotalCuisineFeedback().addAll(waiterReport.getTotalCuisineFeedback());
            existingLocationReport.setMinCuisineFeedback(Math.min(existingLocationReport.getMinCuisineFeedback(), waiterReport.getMinCuisineFeedback()));
            existingLocationReport.setRevenue(existingLocationReport.getRevenue() + waiterReport.getRevenue());
        }
        return locationReportsMap;
    }

    public String generateWaiterCSV(Map<String, WaiterReport> reportDataCurr, Map<String, WaiterReport> reportDataPrev) {
        StringBuilder csvBuilder = new StringBuilder("Location ID,Waiter,Waiter's e-mail,Report period start,Report period end,Waiter working hours,Waiter orders processed,Delta of Waiter Orders processed to previous period in %,Average Service Feedback Waiter (1 to 5),Minimum Service Feedback Waiter (1 to 5),Delta of Average Service Feedback Waiter to previous period in %\n");
        for (WaiterReport report : reportDataCurr.values()) {
            WaiterReport prevReport = reportDataPrev.getOrDefault(report.getWaiterEmail(), WaiterReport.getBlankReport());

            double deltaOrdersProcessed = prevReport.getOrdersProcessed() == 0
                    ? 0
                    : ((report.getOrdersProcessed() - prevReport.getOrdersProcessed()) / prevReport.getOrdersProcessed()) * 100;

            double avgServiceFeedbackCurr = report.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double avgServiceFeedbackPrev = prevReport.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double deltaAvgServiceFeedback = avgServiceFeedbackPrev == 0
                    ? 0
                    : ((avgServiceFeedbackCurr - avgServiceFeedbackPrev) / avgServiceFeedbackPrev) * 100;


            csvBuilder.append(report.getLocationId()).append(",")
                    .append(report.getWaiterName()).append(",")
                    .append(report.getWaiterEmail()).append(",")
                    .append(report.getStartDate()).append(",")
                    .append(report.getEndDate()).append(",")
                    .append(report.getHoursWorked()).append(",")
                    .append(report.getOrdersProcessed()).append(",")
                    .append(String.format("%.2f", deltaOrdersProcessed)).append(",")
                    .append(String.format("%.2f", avgServiceFeedbackCurr)).append(",")
                    .append(report.getMinServiceFeedback()).append(",")
                    .append(deltaAvgServiceFeedback).append("\n");
        }
        return csvBuilder.toString();
    }

    public byte[] generateWaiterPDF(Map<String, WaiterReport> reportDataCurr, Map<String, WaiterReport> reportDataPrev, String startDate, String endDate) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscape);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, landscape.getHeight() - 50);
                contentStream.showText("Waiter Report (" + startDate + "-" + endDate + ")");
                contentStream.endText();

                float yPosition = landscape.getHeight() - 100;
                float margin = 50;
                float tableWidth = landscape.getWidth() - 2 * margin - 50;
                float rowHeight = 15;
                float cellMargin = 2;

                String[] headers = {
                        "Location ID", "Waiter", "Report Start", "Report End",
                        "Hours Worked", "Orders Processed",
                        "Delta Orders (%)", "Avg Service Fdbck.",
                        "Min Service Fbck.", "Delta Serv. Fdbck (%)"
                };

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8);

                for (int i = 0; i < headers.length; i++) {
                    float cellWidth = tableWidth / headers.length;
                    contentStream.addRect(margin + i * cellWidth, yPosition, cellWidth, rowHeight);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 7);
                    contentStream.newLineAtOffset(margin + i * cellWidth + cellMargin, yPosition + cellMargin);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                }
                contentStream.stroke();

                contentStream.setFont(PDType1Font.HELVETICA, 7);
                yPosition -= rowHeight;
                for (WaiterReport report : reportDataCurr.values()) {
                    WaiterReport prevReport = reportDataPrev.getOrDefault(report.getWaiterEmail(), WaiterReport.getBlankReport());

                    double deltaOrdersProcessed = prevReport.getOrdersProcessed() == 0
                            ? 0
                            : ((report.getOrdersProcessed() - prevReport.getOrdersProcessed()) / prevReport.getOrdersProcessed()) * 100;

                    double avgServiceFeedbackCurr = report.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double avgServiceFeedbackPrev = prevReport.getTotalServiceFeedback().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                    double deltaAvgServiceFeedback = avgServiceFeedbackPrev == 0
                            ? 0
                            : ((avgServiceFeedbackCurr - avgServiceFeedbackPrev) / avgServiceFeedbackPrev) * 100;

                    String[] rowData = {
                            report.getLocationId(),
                            report.getWaiterName(),
                            report.getStartDate(),
                            report.getEndDate(),
                            String.valueOf(report.getHoursWorked()),
                            String.valueOf(report.getOrdersProcessed()),
                            String.format("%.2f", deltaOrdersProcessed),
                            String.format("%.2f", avgServiceFeedbackCurr),
                            String.valueOf(report.getMinServiceFeedback()),
                            String.format("%.2f", deltaAvgServiceFeedback)
                    };

                    for (int i = 0; i < rowData.length; i++) {
                        float cellWidth = tableWidth / rowData.length;
                        contentStream.addRect(margin + i * cellWidth, yPosition, cellWidth, rowHeight);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + i * cellWidth + cellMargin, yPosition + cellMargin);
                        contentStream.showText(rowData[i]);
                        contentStream.endText();
                    }
                    contentStream.stroke();
                    yPosition -= rowHeight;

                    if (yPosition < 50) {
                        contentStream.close();
                        page = new PDPage(landscape);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = landscape.getHeight() - 100;
                    }
                }
            } finally {
                contentStream.close();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public String generateLocationCSV(Map<String, LocationReport> reportDataCurr, Map<String, LocationReport> reportDataPrev) {
        StringBuilder csvBuilder = new StringBuilder("Location ID,Report period start,Report period end,Orders processed within location,Delta of orders processed within location to previous period (in %),Average cuisine Feedback by Restaurant location (1 to 5),Minimum cuisine Feedback by Restaurant location (1 to 5),Delta of average cuisine Feedback by Restaurant location to previous period (in %),Revenue for orders within reported period,Delta of revenue for orders to previous period (in %)\n");
        for (LocationReport report : reportDataCurr.values()) {
            LocationReport prevReport = reportDataPrev.getOrDefault(report.getLocationId(), LocationReport.getBlankReport());

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


            csvBuilder.append(report.getLocationId()).append(",")
                    .append(report.getStartDate()).append(",")
                    .append(report.getEndDate()).append(",")
                    .append(report.getOrdersProcessed()).append(",")
                    .append(String.format("%.2f", deltaOrdersProcessed)).append(",")
                    .append(String.format("%.2f", avgCuisineFeedbackCurr)).append(",")
                    .append(report.getMinCuisineFeedback()).append(",")
                    .append(String.format("%.2f", deltaAvgCuisineFeedback)).append(",")
                    .append(report.getRevenue()).append(",")
                    .append(String.format("%.2f", deltaRevenue)).append("\n");
        }
        return csvBuilder.toString();
    }

    public byte[] generateLocationPDF(Map<String, LocationReport> reportDataCurr, Map<String, LocationReport> reportDataPrev, String startDate, String endDate) throws IOException {

        try (PDDocument document = new PDDocument()) {
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(landscape);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);

                contentStream.beginText();
                contentStream.newLineAtOffset(50, landscape.getHeight() - 50);
                contentStream.showText("Location Report (" + startDate + "-" + endDate + ")");
                contentStream.endText();

                float yPosition = landscape.getHeight() - 100;
                float margin = 50;
                float tableWidth = landscape.getWidth() - 2 * margin - 50;
                float rowHeight = 15;
                float cellMargin = 2;

                String[] headers = {
                        "Location ID", "Report Start", "Report End",
                        "Orders Processed", "Delta Orders (%)",
                        "Avg Cuisine Fdbck.", "Min Cuisine Fdbck.",
                        "Delta Cus. Fdbck (%)", "Revenue", "Delta Revenue (%)"
                };

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 8);

                for (int i = 0; i < headers.length; i++) {
                    float cellWidth = tableWidth / headers.length;
                    contentStream.addRect(margin + i * cellWidth, yPosition, cellWidth, rowHeight);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 7);
                    contentStream.newLineAtOffset(margin + i * cellWidth + cellMargin, yPosition + cellMargin);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                }
                contentStream.stroke();

                contentStream.setFont(PDType1Font.HELVETICA, 7);
                yPosition -= rowHeight;
                for (LocationReport report : reportDataCurr.values()) {

                    LocationReport prevReport = reportDataPrev.getOrDefault(report.getLocationId(), new LocationReport(
                            report.getLocationId(), report.getStartDate(), report.getEndDate(), 0, List.of(), 0, 0
                    ));

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

                    String[] rowData = {
                            report.getLocationId(),
                            report.getStartDate(),
                            report.getEndDate(),
                            String.valueOf(report.getOrdersProcessed()),
                            String.format("%.2f", deltaOrdersProcessed),
                            String.format("%.2f", avgCuisineFeedbackCurr),
                            String.valueOf(report.getMinCuisineFeedback()),
                            String.format("%.2f", deltaAvgCuisineFeedback),
                            String.format("%.2f", report.getRevenue()),
                            String.format("%.2f", deltaRevenue)
                    };

                    for (int i = 0; i < rowData.length; i++) {
                        float cellWidth = tableWidth / rowData.length;
                        contentStream.addRect(margin + i * cellWidth, yPosition, cellWidth, rowHeight);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(margin + i * cellWidth + cellMargin, yPosition + cellMargin);
                        contentStream.showText(rowData[i]);
                        contentStream.endText();
                    }
                    contentStream.stroke();
                    yPosition -= rowHeight;

                    if (yPosition < 50) {
                        contentStream.close();
                        page = new PDPage(landscape);
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = landscape.getHeight() - 100;
                    }
                }
            } finally {
                contentStream.close();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    public String uploadToS3(String bucketName, String fileName, String contentType, byte[] data) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);
        s3Client.putObject(bucketName, fileName, new ByteArrayInputStream(data), metadata);

        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withExpiration(new Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000))); // 7 days
        URL url = s3Client.generatePresignedUrl(urlRequest);
        return url.toString();
    }

    public static double roundToSingleDecimal(double value) {
        // Convert the double to BigDecimal, set scaling to 1 decimal place, and round
        BigDecimal roundedValue = new BigDecimal(value).setScale(1, RoundingMode.HALF_UP);
        return roundedValue.doubleValue();
    }

    public void updateReportWithUpdatedFeedback(String waiterEmail, String date, BigDecimal oldServiceRating, BigDecimal newServiceRating,BigDecimal oldCuisineRating, BigDecimal newCuisineRating) throws Exception {
        try {
            String reportKey = waiterEmail + "#" + date;
            Item reportItem = reportsTable.getItem("report_id", reportKey);

            List<Double> servicefeedbackList = new ArrayList<BigDecimal>(reportItem.getList("total_service_feedback")).stream().map(BigDecimal::doubleValue).collect(Collectors.toList());
            List<Double> cuisinefeedbackList = new ArrayList<BigDecimal>(reportItem.getList("total_cuisine_feedback")).stream().map(BigDecimal::doubleValue).collect(Collectors.toList());
            Double newMinService=6.0D;
            Double newMinCuisine=6.0D;

            if(!servicefeedbackList.isEmpty() && servicefeedbackList.contains(roundToSingleDecimal(oldServiceRating.doubleValue()))) {
                System.out.println(servicefeedbackList.remove(roundToSingleDecimal(oldServiceRating.doubleValue()))); // Remove the old rating
                servicefeedbackList.add(roundToSingleDecimal(newServiceRating.doubleValue()));   // Add the new rating
            }
            if (!cuisinefeedbackList.isEmpty() && cuisinefeedbackList.contains(roundToSingleDecimal(oldCuisineRating.doubleValue()))) {
                System.out.println(cuisinefeedbackList.remove(roundToSingleDecimal(oldCuisineRating.doubleValue()))); // Remove the old rating
                cuisinefeedbackList.add(roundToSingleDecimal(newCuisineRating.doubleValue()));   // Add the new rating
            }

            // Adjust minimum feedback
            Double currentMinService = reportItem.getNumber("minimum_service_feedback").doubleValue();
            if (Objects.equals(currentMinService, oldServiceRating.doubleValue()) || Double.valueOf(newServiceRating.doubleValue()).compareTo(currentMinService) < 0) {
                         newMinService = servicefeedbackList.stream()
                        .min(Double::compareTo)
                        .orElse(roundToSingleDecimal(newServiceRating.doubleValue())); // Find the new minimum
            }

            Double currentMinCuisine = reportItem.getNumber("minimum_cuisine_feedback").doubleValue();
            if (Objects.equals(currentMinCuisine, roundToSingleDecimal(oldCuisineRating.doubleValue())) || Double.valueOf(newCuisineRating.doubleValue()).compareTo(currentMinCuisine) < 0) {
                         newMinCuisine = cuisinefeedbackList.stream()
                        .min(Double::compareTo)
                        .orElse(roundToSingleDecimal(newCuisineRating.doubleValue())); // Find the new minimum
            }


            String updateExpression =  "SET total_service_feedback = :total_service_feedback," +
                    "minimum_service_feedback = :minimum_service_feedback," +
                    "total_cuisine_feedback = :total_cuisine_feedback," +
                    "minimum_cuisine_feedback = :minimum_cuisine_feedback";

            Map<String, Object> valueMap = Map.of(
                    ":total_service_feedback", servicefeedbackList,
                    ":minimum_service_feedback", BigDecimal.valueOf(roundToSingleDecimal(newMinService)),
                    ":total_cuisine_feedback", cuisinefeedbackList,
                    ":minimum_cuisine_feedback", BigDecimal.valueOf(roundToSingleDecimal(newMinCuisine))
            );

            reportsTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("report_id", waiterEmail + "#" + date)
                    .withUpdateExpression(updateExpression)
                    .withValueMap(valueMap)
            );
        } catch (Exception e) {
            throw new Exception("Error updating the report's table: " + e.getMessage() + Arrays.toString(e.getStackTrace()));
        }
    }
}
