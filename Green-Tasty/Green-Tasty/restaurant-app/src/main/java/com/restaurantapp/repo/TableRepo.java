package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.restaurantapp.dto.Tables;
import com.restaurantapp.service.TableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TableRepo {
    AmazonDynamoDB amazonDynamoDBClient;
    DynamoDB dynamoDBClient;
    Table tablesTable;
    private final Logger logger;

    @Value("${tables.table}")
    private String tablesTableName;

    public TableRepo(
            AmazonDynamoDB amazonDynamoDBClient,
            String tablesTableName
    ) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDBClient = new DynamoDB(amazonDynamoDBClient);
        this.tablesTable = dynamoDBClient.getTable(tablesTableName);
        logger = LoggerFactory.getLogger(TableService.class);
    }

    public boolean isTimePresent(String locationId, String tableId, String timeSlot) {
        Item item = tablesTable.getItem("location_id", locationId, "table_number", tableId);
        Map<String, Boolean> availableSlots = (Map<String, Boolean>) item.get("available_slots");
        return availableSlots.containsKey(timeSlot);
    }

    public boolean isTimeAvailable(String locationId, String tableId, String timeSlot) {
        Item item = tablesTable.getItem("location_id", locationId, "table_number", tableId);
        Map<String, Boolean> availableSlots = (Map<String, Boolean>) item.get("available_slots");
        return availableSlots.get(timeSlot);
    }

    public List<Tables> getAvailableTables(String locationId, String date, String time, String guests, String zone) throws Exception {
        try {
            QueryRequest queryRequest = null;
            Map<String, String> expressionAttributeNames = null;
            Map<String, AttributeValue> expressionAttributeValues = null;
            String filter = null;
            if (guests != null) {
                expressionAttributeValues = Map.of(
                        ":location_id", new AttributeValue().withS(locationId),
                        ":date", new AttributeValue().withS(date),
                        ":capacity", new AttributeValue().withN(guests)
                );
                expressionAttributeNames = Map.of("#dateAlias", "date", "#capacityAlias", "capacity");
                filter = "#dateAlias = :date AND #capacityAlias >= :capacity";
            } else {
                expressionAttributeValues = Map.of(
                        ":location_id", new AttributeValue().withS(locationId),
                        ":date", new AttributeValue().withS(date)
                );
                expressionAttributeNames = Map.of("#dateAlias", "date");
                filter = "#dateAlias = :date";
            }

            queryRequest = new QueryRequest()
                    .withTableName(tablesTableName)
                    .withKeyConditionExpression("location_id = :location_id")
                    .withFilterExpression(filter)
                    .withExpressionAttributeNames(expressionAttributeNames)
                    .withExpressionAttributeValues(expressionAttributeValues);

            QueryResult queryResult = amazonDynamoDBClient.query(queryRequest);

            List<Map<String, AttributeValue>> items = queryResult.getItems();
            if (items.isEmpty()) return new ArrayList<>();

            List<Tables> result = new ArrayList<>();
            LocalDateTime requestedDateTime = null;
            if (time != null)
                requestedDateTime = LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")));

            LocalTime currTime = LocalTime.now(ZoneId.of(zone));
            LocalDate currDate = LocalDate.now(ZoneId.of(zone));
            LocalDateTime currDateTime = LocalDateTime.of(currDate, currTime);

            for (Map<String, AttributeValue> item : items) {
                String tableNumber = item.get("table_number").getS();
                String locationID = item.get("location_id").getS();
                String capacity = item.get("capacity").getN();
                String dateTable = item.get("date").getS();

                Map<String, AttributeValue> rawAvailableSlots = item.get("available_slots").getM();
                Map<String, Boolean> availableSlots = new HashMap<>();
                for (Map.Entry<String, AttributeValue> entry : rawAvailableSlots.entrySet()) {
                    availableSlots.put(entry.getKey(), entry.getValue().getBOOL());
                }

                Map<String, Boolean> matchingTimeRanges = new TreeMap<>();
                for (Map.Entry<String, Boolean> entry : availableSlots.entrySet()) {
                    String timeRange = entry.getKey();
                    boolean isAvailable = entry.getValue();

                    String[] times = timeRange.split("-");
                    LocalTime startTime = LocalTime.parse(times[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy")), startTime);

                    if (isAvailable) {
                        if (time != null) {
                            if ((requestedDateTime.isBefore(startDateTime) || requestedDateTime.equals(startDateTime))
                                    && !requestedDateTime.isBefore(currDateTime)) {
                                matchingTimeRanges.put(timeRange, true);
                            }
                        } else if (startDateTime.isAfter(currDateTime)) {
                            matchingTimeRanges.put(timeRange, true);
                        }
                    }
                }
                if (!matchingTimeRanges.isEmpty()) {
                    Tables tableDetails = new Tables(tableNumber, locationID, capacity, dateTable, matchingTimeRanges);
                    result.add(tableDetails);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.trace(Arrays.toString(e.getStackTrace()));
            throw new Exception("Error checking table availability.");
        }
    }

    public void updateTable(String tableId, String locationId, String date, String timeSlot, boolean isAvailable) throws Exception {
        try {
            UpdateItemOutcome outcome = tablesTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("location_id", locationId, "table_number", tableId)
                    .withUpdateExpression("SET #mapKey.#key = :newValue")
                    .withNameMap(Map.of(
                            "#mapKey", "available_slots",
                            "#key", timeSlot)
                    )
                    .withValueMap(Map.of(
                            ":newValue", isAvailable)
                    )
                    .withReturnValues(ReturnValue.UPDATED_NEW)
            );
            logger.info("Update successful: {}", outcome.getItem());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new Exception("Failed to update table details for Id: " + tableId);
        }
    }
}
