package com.reportsapp.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.reportsapp.repo.BookingRepo;
import com.reportsapp.repo.DishRepo;
import com.reportsapp.repo.FeedbackRepo;
import com.reportsapp.repo.ReportsRepo;
import com.reportsapp.util.DateFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.awspring.cloud.sqs.annotation.SqsListener;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ReportsHandlerService {
    private ReportsRepo reportsRepo;
    private BookingRepo bookingRepo;
    private DishRepo dishRepo;
    private FeedbackRepo feedbackRepo;
    private final Logger logger;

    @Autowired
    public ReportsHandlerService(ReportsRepo reportsRepo, BookingRepo bookingRepo, DishRepo dishRepo, FeedbackRepo feedbackRepo) {
        this.reportsRepo = reportsRepo;
        this.bookingRepo = bookingRepo;
        this.dishRepo = dishRepo;
        this.feedbackRepo = feedbackRepo;
        this.logger = LoggerFactory.getLogger(ReportsHandlerService.class);
    }

    @SqsListener("tm9-report-info-queue-test")
    public void handleQueueMessage(String message) {
        try {
            logger.info("Received message from queue: {}", message);
            updateReportsTable(new JSONObject(message));
        } catch (Exception e) {
            logger.error("An unknown error occurred: {}", e.getMessage());
            logger.error("Stack Trace: {}", Arrays.toString(e.getStackTrace()));
        }

        logger.info("Processed messages from queue successfully");
    }

    public void updateReportsTable(JSONObject reservation) throws Exception {

        String waiterId = reservation.getString("waiter_email");
        String locationId = reservation.getString("location_id");
        String date = reservation.getString("date");

        Item feedbackItem = null;
        String feedbackId = reservation.getString("feedback_id");
        if (feedbackId != null && !feedbackId.isBlank())
            feedbackItem = feedbackRepo.getItem(feedbackId);


        Item reportItem = reportsRepo.getItem(waiterId, date);
        BigDecimal revenue = calculateRevenueForOrder(parseJSONMap(reservation.get("pre_order").toString()));
        if (reportItem == null) {
            reportsRepo.putItem(new Item()
                    .withPrimaryKey("report_id", waiterId + "#" + date)
                    .withString("location_id", locationId)
                    .withString("date", DateFormatter.convertToISOFormat(date))
                    .withString("waiter_email", waiterId)
                    .withNumber("waiter_working_hours", 1.5)
                    .withNumber("waiter_orders_processed", 1)
                    .withList("total_service_feedback", feedbackItem != null ? feedbackItem.getNumber("service_rating") : 0)
                    .withNumber("minimum_service_feedback", feedbackItem != null ? feedbackItem.getNumber("service_rating") : 0)
                    .withList("total_cuisine_feedback", feedbackItem != null ? feedbackItem.getNumber("cuisine_rating") : 0)
                    .withNumber("minimum_cuisine_feedback", feedbackItem != null ? feedbackItem.getNumber("cuisine_rating") : 0)
                    .withNumber("total_revenue", revenue)
            );
        } else {
            BigDecimal waiterWorkingHours;
            if (isWorkingDiffSlot(waiterId, reservation.getString("time_slot")))
                waiterWorkingHours = reportItem.getNumber("waiter_working_hours").add(BigDecimal.valueOf(1.5));
            else waiterWorkingHours = reportItem.getNumber("waiter_working_hours");

            String updateExpression = "SET waiter_working_hours = :waiter_working_hours," +
                    "waiter_orders_processed = :waiter_orders_processed," +
                    "total_revenue = :total_revenue";
            Map<String, Object> valueMap = null;
            if (feedbackItem == null || feedbackItem.getNumber("service_rating").equals(BigDecimal.ZERO)) {
                valueMap = Map.of(
                        ":waiter_working_hours", waiterWorkingHours,
                        ":waiter_orders_processed", reportItem.getNumber("waiter_orders_processed").add(BigDecimal.ONE),
                        ":total_revenue", reportItem.getNumber("total_revenue").add(revenue)
                );
            } else {

                List<BigDecimal> totalServiceFeedback = new ArrayList<>(reportItem.getList("total_service_feedback"));
                totalServiceFeedback.add(feedbackItem.getNumber("service_rating"));
                List<BigDecimal> totalCuisineFeedback = new ArrayList<>(reportItem.getList("total_cuisine_feedback"));
                totalCuisineFeedback.add(feedbackItem.getNumber("cuisine_rating"));

                BigDecimal minServiceFeedback = reportItem.getNumber("minimum_service_feedback").equals(BigDecimal.ZERO)
                        ? feedbackItem.getNumber("service_rating")
                        : reportItem.getNumber("minimum_service_feedback").min(feedbackItem.getNumber("service_rating"));
                BigDecimal minCuisineFeedback = reportItem.getNumber("minimum_cuisine_feedback").equals(BigDecimal.ZERO)
                        ? feedbackItem.getNumber("cuisine_rating")
                        : reportItem.getNumber("minimum_cuisine_feedback").min(feedbackItem.getNumber("cuisine_rating"));


                updateExpression = updateExpression +
                        ",total_service_feedback = :total_service_feedback," +
                        "minimum_service_feedback = :minimum_service_feedback," +
                        "total_cuisine_feedback = :total_cuisine_feedback," +
                        "minimum_cuisine_feedback = :minimum_cuisine_feedback";
                valueMap = Map.of(
                        ":waiter_working_hours", waiterWorkingHours,
                        ":waiter_orders_processed", reportItem.getNumber("waiter_orders_processed").add(BigDecimal.ONE),
                        ":total_service_feedback", totalServiceFeedback.stream().toList(),
                        ":minimum_service_feedback", minServiceFeedback,
                        ":total_cuisine_feedback", totalCuisineFeedback.stream().toList(),
                        ":minimum_cuisine_feedback", minCuisineFeedback,
                        ":total_revenue", reportItem.getNumber("total_revenue").add(revenue)
                );
                reportsRepo.updateTable(waiterId, date, updateExpression, valueMap);
            }
        }
    }

    private boolean isWorkingDiffSlot(String waiterId, String timeSlot) {
        return bookingRepo.getItems(waiterId, timeSlot).size() == 1;
    }

    private BigDecimal calculateRevenueForOrder(Map<String, String> preOrder) throws Exception {
        BigDecimal revenue = BigDecimal.ZERO;
        try {
            for (String dishId : preOrder.keySet()) {
                BigDecimal dishPrice = BigDecimal.valueOf(dishRepo.getDishPrice(dishId));
                BigDecimal quantity = BigDecimal.valueOf(Long.parseLong(preOrder.get(dishId)));
                revenue = revenue.add(dishPrice.multiply(quantity));
            }
        } catch (Exception e) {
            throw new Exception("Failed to fetch dishes.", e.getCause());
        }
        return revenue;
    }

    private Map<String, String> parseJSONMap(String jsonMap) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonMap);
        Map<String, String> resultMap = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode valueNode = field.getValue();

            // Check if the value is an object with an "s" field
            if (valueNode.isObject() && valueNode.has("s")) {
                resultMap.put(key, valueNode.get("s").asText());
            }
            // Otherwise, assume the value is a plain string
            else if (valueNode.isTextual()) {
                resultMap.put(key, valueNode.asText());
            } else {
                // Handle unexpected formats (optional)
                throw new IllegalArgumentException("Unexpected JSON format for key: " + key);
            }
        }
        return resultMap;
    }
}
