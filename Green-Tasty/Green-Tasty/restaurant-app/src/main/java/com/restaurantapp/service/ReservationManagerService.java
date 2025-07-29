package com.restaurantapp.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.restaurantapp.dto.ReservationStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReservationManagerService {
    private static final Logger logger = LoggerFactory.getLogger(ReservationManagerService.class);

    private final AmazonDynamoDB amazonDynamoDBClient;
    private final AmazonSQS amazonSQSClient;

    @Value("${reservation.table}")
    private String reservationTable;

    @Value("${waiter.table}")
    private String waiterTable;

    @Value("${aws.account.id}")
    private String accountId;

    @Value("${aws.region}")
    private String region;

    @Value("${report_info_queue}")
    private String reportInfoQueue;

    @Value("${location.table}")
    private String locationTable;

    @Autowired
    public ReservationManagerService(AmazonDynamoDB amazonDynamoDBClient, AmazonSQS amazonSQSClient) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.amazonSQSClient = amazonSQSClient;
    }

    @Scheduled(cron = "0 */15 * * * *")// Runs every 5 minutes - adjust as needed
    public void processReservations() {
        try {
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(reservationTable)
                    .withFilterExpression("#status = :status1 OR #status = :status2")
                    .withExpressionAttributeNames(Map.of("#status", "status"))
                    .withExpressionAttributeValues(Map.of(":status1", new AttributeValue().withS("RESERVED"),
                            ":status2", new AttributeValue().withS("IN_PROGRESS")));

            ScanResult scanResult = amazonDynamoDBClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            for (Map<String, AttributeValue> item : items) {
                ReservationStatus reservationStatus = ReservationStatus.valueOf(item.get("status").getS());
                String date = item.get("date").getS();
                String zoneId = getZoneFromLocation(item.get("location_id"));

                String timeSlot = item.get("time_slot").getS();
                String[] time = timeSlot.split("-");
                LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(ZoneId.of(zoneId)), LocalTime.now(ZoneId.of(zoneId)));
                LocalDateTime dateTimeFrom = LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy")), LocalTime.parse(time[0]));
                LocalDateTime dateTimeTo = LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy")), LocalTime.parse(time[1]));

                if (currentDateTime.isBefore(dateTimeFrom)) reservationStatus = ReservationStatus.RESERVED;
                else if (currentDateTime.isBefore(dateTimeTo)) reservationStatus = ReservationStatus.IN_PROGRESS;
                else if (currentDateTime.isAfter(dateTimeTo)) {
                    reservationStatus = ReservationStatus.PENDING_REVIEW;
                    sendMessageToQueue(item);
                }
                updateReservationStatus(item.get("reservation_id"), item.get("waiter_email"), item.get("customer_email"), reservationStatus.name());
            }

        } catch (Exception e) {
            logger.error("Failed to update table Reservation: {}", e.getMessage(), e);
        }
        logger.info("Updated table Reservation successfully");
    }

    private String getZoneFromLocation(AttributeValue locationId) {
        return amazonDynamoDBClient.getItem(new GetItemRequest()
                        .withTableName(locationTable)
                        .withProjectionExpression("#zone")
                        .withExpressionAttributeNames(Map.of("#zone", "zone"))
                        .withKey(Map.of(
                                "location_id", locationId)))
                .getItem()
                .get("zone")
                .getS();
    }

    private void sendMessageToQueue(Map<String, AttributeValue> item) {
        AttributeValue customerEmail = item.get("customer_email");
        AttributeValue visitorId = item.get("visitor_id");

        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl("https://sqs." + region + ".amazonaws.com/" + accountId + "/" + reportInfoQueue)
                .withMessageBody(new JSONObject().put("date", item.get("date").getS())
                        .put("reservation_id", item.get("reservation_id").getS())
                        .put("waiter_email", item.get("waiter_email").getS())
                        .put("customer_email", customerEmail != null ? customerEmail.getS() : "")
                        .put("visitor_id", visitorId != null ? visitorId.getS() : "")
                        .put("feedback_id", item.get("feedback_id").getS())
                        .put("pre_order", item.get("pre_order").getM())
                        .put("table_id", item.get("table_id").getL())
                        .put("location_id", item.get("location_id").getS())
                        .put("status", item.get("status").getS())
                        .put("time_slot", item.get("time_slot").getS())
                        .put("guests_number", item.get("guests_number").getN())
                        .toString()
                );
        amazonSQSClient.sendMessage(sendMessageRequest);
    }

    private void updateReservationStatus(AttributeValue reservationId, AttributeValue waiterId, AttributeValue customerId, String newStatus) throws Exception {
        if (newStatus.equals("PENDING_REVIEW")) {
            GetItemRequest waiterGetItemRequest = new GetItemRequest()
                    .withTableName(waiterTable)
                    .withKey(Map.of("email", waiterId));

            Map<String, AttributeValue> waiterItem = amazonDynamoDBClient.getItem(waiterGetItemRequest).getItem();

            if (waiterItem == null) {
                throw new Exception("Waiter ID: " + waiterId + " not found in Waiter Table.");
            }
            Map<String, AttributeValueUpdate> waiterUpdateValues;
            if (customerId == null) {
                int currCount = Integer.parseInt(waiterItem.get("visitor_count").getN());
                String visitorCount = String.valueOf(currCount - 1);
                waiterUpdateValues = Map.of(
                        "visitor_count", new AttributeValueUpdate()
                                .withValue(new AttributeValue().withN(visitorCount))
                                .withAction("PUT")
                );
            } else {
                LocalDate localDate = LocalDate.now();
                DayOfWeek dayOfWeek = localDate.getDayOfWeek();
                int index = dayOfWeek.getValue() - 1;

                List<Integer> customerCount = waiterItem.get("customer_count").getL().stream()
                        .map(AttributeValue::getN)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                customerCount.set(index, customerCount.get(index) - 1);

                waiterUpdateValues = Map.of(
                        "customer_count", new AttributeValueUpdate()
                                .withValue(new AttributeValue().withL(customerCount.stream().map(x -> new AttributeValue().withN(x.toString())).toList()))
                                .withAction("PUT")
                );
            }

            UpdateItemRequest waiterUpdateItemRequest = new UpdateItemRequest()
                    .withTableName(waiterTable)
                    .withKey(Map.of("email", waiterId))
                    .withAttributeUpdates(waiterUpdateValues);

            amazonDynamoDBClient.updateItem(waiterUpdateItemRequest);
        }

        // Update the reservation table
        Map<String, AttributeValueUpdate> reservationUpdateValues = Map.of(
                "status", new AttributeValueUpdate()
                        .withValue(new AttributeValue().withS(newStatus))
                        .withAction("PUT")
        );
        UpdateItemRequest reservationUpdateItemRequest = new UpdateItemRequest()
                .withTableName(reservationTable)
                .withKey(Map.of("reservation_id", reservationId))
                .withAttributeUpdates(reservationUpdateValues);
        amazonDynamoDBClient.updateItem(reservationUpdateItemRequest);
    }
}