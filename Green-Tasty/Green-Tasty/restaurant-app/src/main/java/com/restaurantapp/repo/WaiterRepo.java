package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.restaurantapp.dto.SignUp;
import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.exception.PasswordMismatchException;
import com.restaurantapp.exception.UserDoesNotExistsException;
import com.restaurantapp.exception.WaiterNotFoundException;
import com.restaurantapp.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class WaiterRepo {
    AmazonDynamoDB amazonDynamoDBClient;
    DynamoDB dynamoDBClient;
    Table waiterTable;
    LocationRepo locationRepo;

    @Value("${waiter.table}")
    private String waiterTableName;

    public WaiterRepo(AmazonDynamoDB amazonDynamoDBClient, LocationRepo locationRepo, String waiterTableName) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDBClient = new DynamoDB(amazonDynamoDBClient);
        this.waiterTable = dynamoDBClient.getTable(waiterTableName);
        this.locationRepo = locationRepo;
    }

    public boolean isWaiter(String email) throws Exception {
        try {
            Item item = waiterTable.getItem("email", email);
            return item != null;
        } catch (Exception e) {
            throw new Exception("Error checking entry: " + e.getMessage());
        }
    }

    public boolean isWaiterSignedUp(String email) throws Exception {
        try {
            Item item = waiterTable.getItem("email", email);
            return item.getString("first_name") != null;
        } catch (Exception e) {
            throw new Exception("Error checking entry: " + e.getMessage());
        }
    }

    public void updateWaiterDetails(SignUp signUp) throws Exception {
        try {
            UpdateItemOutcome outcome = waiterTable.updateItem(
                    "email", signUp.getEmail(), // Primary key (partition key)
                    "SET #first_name = :first_name, #last_name = :last_name, #password = :password" +
                            "#customer_count = :customer_count, #visitor_count = :visitor_count, #image_url = :image_url", // Update expression
                    new HashMap<String, String>() {{
                        put("#first_name", "first_name");
                        put("#last_name", "last_name");
                        put("#password", "password");
                        put("#customer_count", "customer_count");
                        put("#visitor_count", "visitor_count");
                        put("#image_url", "image_url");
                    }},
                    new HashMap<String, Object>() {{
                        put(":first_name", signUp.getFirstName());
                        put(":last_name", signUp.getLastName());
                        put(":password", signUp.getPassword());
                        put(":customer_count", new ArrayList<>(Collections.nCopies(7, 0)));
                        put(":visitor_count", 1);
                        put(":image_url", "images/profile/default.jpg");
                    }}
            );

            System.out.println("Update succeeded: " + outcome.getUpdateItemResult());
        } catch (Exception e) {
            throw new Exception("Failed to update waiter details: " + e.getMessage());
        }

    }

    public Map<String, Object> getWaiterDetails(String email) throws WaiterNotFoundException {
        Item item = waiterTable.getItem("email", email);
        if (item == null) throw new WaiterNotFoundException("Waiter with email " + email + " not found.");
        return item.asMap();
    }

    public String getLeastBusyWaiter(String locationId, String date) throws Exception {
        try {
            Map<String, AttributeValue> expressionAttributeValues = Map.of(":location_id", new AttributeValue().withS(locationId));
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(waiterTableName)
                    .withFilterExpression("location_id = :location_id")
                    .withExpressionAttributeValues(expressionAttributeValues);

            ScanResult scanResult = amazonDynamoDBClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();

            if (items == null || items.isEmpty()) throw new Exception("No waiters available.");

            String email = "";
            int min = Integer.MAX_VALUE;

            String zoneId = locationRepo.getLocationById(locationId).getZone();
            LocalDate currentDate = LocalDate.now(ZoneId.of(zoneId));
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            int index = dayOfWeek.getValue() - 1;

            boolean currDay = localDate.equals(currentDate);

            for (Map<String, AttributeValue> item : items) {
                int visitorCount = 0;
                if (currDay) {
                    visitorCount = Integer.parseInt(item.get("visitor_count").getN());
                }
                List<Integer> countCustomer = item.get("customer_count").getL().stream()
                        .map(AttributeValue::getN)
                        .map(Integer::parseInt)
                        .toList();

                int customerCount = countCustomer.get(index);
                String waiterEmail = item.get("email").getS();

                if (min > customerCount + visitorCount) {
                    min = customerCount + visitorCount;
                    email = waiterEmail;
                }
            }
            return email;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateWaiterBusyCount(String waiterEmail, String date, String countName, int change) throws Exception {
        try {
            ValueMap valueMap = null;
            if (countName.equals("visitor_count")) {
                int currCount = waiterTable.getItem("email", waiterEmail).getNumber(countName).intValue();
                valueMap = new ValueMap().with(":newCount", currCount + change);
            } else {
                LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                DayOfWeek dayOfWeek = localDate.getDayOfWeek();
                int index = dayOfWeek.getValue() - 1;

                Item item = waiterTable.getItem("email", waiterEmail);
                List<BigDecimal> x = item.getList("customer_count");
                List<Integer> customerCount = x.stream()
                        .map(BigDecimal::intValue)
                        .collect(Collectors.toList());
                customerCount.set(index, customerCount.get(index) + change);
                valueMap = new ValueMap().with(":newCount", customerCount);
            }
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("email", waiterEmail)
                    .withUpdateExpression("SET " + countName + " = :newCount")
                    .withValueMap(valueMap);
            waiterTable.updateItem(updateItemSpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateWaiterInfo(String email, String firstName, String lastName, String imageUrl) throws Exception {
        try {
            String addToExpression = "";
            Map<String, Object> valueMap = new HashMap<String, Object>() {{
                put(":first_name", firstName);
                put(":last_name", lastName);
            }};
            Map<String, String> nameMap = new HashMap<String, String>() {{
                put("#first_name", "first_name");
                put("#last_name", "last_name");
            }};
            if (imageUrl != null && !imageUrl.isBlank()) {
                addToExpression = ", #image_url = :image_url";
                nameMap.put("#image_url", "image_url");
                valueMap.put(":image_url", imageUrl);
            }
            UpdateItemOutcome outcome = waiterTable.updateItem(
                    "email", email, // Primary key (partition key)
                    "SET #first_name = :first_name, #last_name = :last_name" + addToExpression, // Update expression
                    nameMap,
                    valueMap
            );

            System.out.println("Update succeeded: " + outcome.getUpdateItemResult());
        } catch (Exception e) {
            throw new Exception("Failed to update waiter details: " + e.getMessage());
        }

    }

    public String getVisitorCount(String waiterEmail) {
        return waiterTable.getItem("email", waiterEmail).getNumber("visitor_count").toString();
    }

    public void updateWaiterAverageRatings(String waiterEmail, float newServiceRating) throws Exception {
        try {

            Item waiterItem = waiterTable.getItem("email", waiterEmail);

            if (waiterItem == null) {
                throw new WaiterNotFoundException("Waiter not found with ID: " + waiterEmail);
            }

            float currentAverageRating = waiterItem.getFloat("waiter_rating");
            float totalRatingsCount = waiterItem.getFloat("total_rating_count");
            float rawNewAverage = (currentAverageRating * totalRatingsCount + newServiceRating) / (totalRatingsCount + 1);
            // Round to 2 decimal places
            BigDecimal newAverageRating = BigDecimal.valueOf(rawNewAverage).setScale(2, RoundingMode.HALF_UP);
            waiterTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("email", waiterEmail)
                    .withUpdateExpression("SET waiter_rating = :avg, total_rating_count = :count")
                    .withValueMap(new ValueMap()
                            .withNumber(":avg", newAverageRating)
                            .withNumber(":count", totalRatingsCount + 1))
            );

        } catch (Exception e) {
            throw new RuntimeException("Error updating waiter ratings: " + e.getMessage());
        }
    }

    public void adjustWaiterServiceRating(String waiterEmail, float oldServiceRating, float newServiceRating) throws Exception {
        try {
            Item waiterItem = waiterTable.getItem("email", waiterEmail);

            if (waiterItem == null) {
                throw new WaiterNotFoundException("Waiter not found with ID: " + waiterEmail);
            }

            float currentAverageRating = waiterItem.getFloat("waiter_rating");
            float totalRatingsCount = waiterItem.getFloat("total_rating_count");

            float ratingDifference = newServiceRating - oldServiceRating;
            float rawAdjustedAverage = currentAverageRating + (ratingDifference / totalRatingsCount);

            // Round to 2 decimal places using BigDecimal
            BigDecimal adjustedAverage = BigDecimal.valueOf(rawAdjustedAverage)
                    .setScale(2, RoundingMode.HALF_UP);

            waiterTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("email", waiterEmail)
                    .withUpdateExpression("SET waiter_rating = :avg")
                    .withValueMap(new ValueMap()
                            .withNumber(":avg", adjustedAverage))
            );

            System.out.println("Waiter ratings adjusted successfully for Waiter ID: " + waiterEmail);
        } catch (Exception e) {
            throw new Exception("Error adjusting waiter ratings: " + e.getMessage(), e);
        }
    }

    public void updateWaiterPassword(String email, UpdatePasswordRequest request) throws Exception {
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();

        Item item = waiterTable.getItem("email", email);
        if (item == null) {
            throw new UserDoesNotExistsException("Waiter not found with email: " + email);
        }

        String storedHashedPassword = item.getString("password");

        if (!BCrypt.checkpw(oldPassword, storedHashedPassword)) {
            throw new PasswordMismatchException("Old password does not match.");
        }

        boolean isValidPassword = ValidationUtil.isValidPassword(newPassword);

        if (!isValidPassword) {
            throw new IllegalArgumentException("New password does not match with the expected criteria.");
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("email", email)
                .withUpdateExpression("SET password = :password")
                .withValueMap(new ValueMap().withString(":password", newHashedPassword));

        waiterTable.updateItem(updateItemSpec);
    }
}
