package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.restaurantapp.dto.Dish;
import com.restaurantapp.exception.DishNotFoundException;
import com.restaurantapp.exception.ReservationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DishRepo {
    AmazonDynamoDB amazonDynamoDBClient;
    DynamoDB dynamoDBClient;
    Table dishTable;
    Table reservationTable;
    Table dishLocationTable;

    @Autowired
    public DishRepo(
            AmazonDynamoDB amazonDynamoDBClient,
            String dishTableName,
            String reservationTableName,
            String dishLocationTableName
    ) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDBClient = new DynamoDB(amazonDynamoDBClient);
        this.dishTable = dynamoDBClient.getTable(dishTableName);
        this.reservationTable = dynamoDBClient.getTable(reservationTableName);
        this.dishLocationTable = dynamoDBClient.getTable(dishLocationTableName);
    }

    // getting popular dishes
    public List<Map<String, Object>> getPopularDishes() throws DishNotFoundException {
        ItemCollection<ScanOutcome> items = dishTable.scan();

        List<Map<String, Object>> popularDishes = StreamSupport.stream(items.spliterator(), false)
                .map(item -> Map.of(
                        "name", item.getString("name"),
                        "price", item.getString("price"),
                        "weight", item.getString("weight"),
                        "imageUrl", item.getString("imageUrl"),
                        "orderCount", (Object) item.getInt("orderCount")
                ))
                .sorted((dish1, dish2) -> Integer.compare(
                        Integer.parseInt(dish2.get("orderCount").toString()),
                        Integer.parseInt(dish1.get("orderCount").toString())
                ))
                .limit(20)
                .collect(Collectors.toList());

        if (popularDishes.isEmpty()) {
            throw new DishNotFoundException("No popular dishes were found in the database.");
        }

        return popularDishes;
    }

    public boolean checkIfDishIdExists(String dishId) throws Exception {

        Item item = dishTable.getItem("id", dishId);
        if (item == null) {
            throw new DishNotFoundException("Dish not found for dishId: " + dishId);
        }
        return true;


    }

    // getting dish by id
    // Fetching dish by ID with improved exception handling
    public Dish getDishById(String dishId) throws DishNotFoundException, Exception {
        try {
            // Query item from DynamoDB
            Item item = dishTable.getItem("id", dishId);
            if (item == null) {
                // Throw custom exception if dish is not found
                throw new DishNotFoundException("Dish not found for dishId: " + dishId);
            }

            // Convert item to Dish object
            return Dish.fromMap(item.asMap());

        } catch (DishNotFoundException e) {
            // Re-throw the DishNotFoundException for the handler to catch
            throw e;
        } catch (Exception e) {
            // Handle unexpected errors
            throw new Exception("Error fetching dish for dishId: " + dishId + ", " + e.getMessage());
        }
    }

    public Map<String,Object> getDishByIdForLocation(String dishId)throws DishNotFoundException {
        try {
            Item item = dishTable.getItem("id", dishId);
            if (item == null) {
                throw new DishNotFoundException("Dish not found for dishId: " + dishId);
            }

            Map<String, Object> dishMap = new HashMap<>();
            dishMap.put("name", item.getString("name"));
            dishMap.put("price", item.getString("price"));
            dishMap.put("weight", item.getString("weight"));
            dishMap.put("imageUrl", item.getString("imageUrl"));

            return dishMap;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching dish for dishId: " + dishId );
        }
    }


    public List<Map<String, Object>> getDishes(String dishType, String sort) throws Exception {
        List<Map<String, Object>> dishes = new ArrayList<>();

        try {
            // Step 1: Prepare the ScanRequest for DynamoDB
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(dishTable.getTableName()) // Table name from environment variable
                    .withProjectionExpression("id, #name, imageUrl, price, #state, weight, orderCount") // Fields to retrieve
                    .withExpressionAttributeNames(Map.of(
                            "#name", "name",
                            "#state", "state"
                    ));

            // Step 2: Add filtering logic if dishType is provided
            if (dishType != null && !dishType.isEmpty()) {
                // Transform the dishType value into a properly formatted string
                dishType = dishType.replaceAll("[_+]", " ");
                dishType = Arrays.stream(dishType.split("\\s+"))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                        .collect(Collectors.joining(" "));

                // Add filter expression to ScanRequest
                Map<String, AttributeValue> expressionAttributeValues = Map.of(
                        ":dishType", new AttributeValue().withS(dishType)
                );

                scanRequest
                        .withFilterExpression("dishType = :dishType") // Only retrieve items matching dishType
                        .withExpressionAttributeValues(expressionAttributeValues);
            }

            // Step 3: Execute ScanRequest and process results
            ScanResult scanResult = amazonDynamoDBClient.scan(scanRequest);
            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                dishes.add(convertToMap(item)); // Convert results to a Java-friendly Map
            }

            // Step 4: Sort dishes if a sort parameter is provided
            sortDishes(dishes, sort);

            return dishes;

        } catch (Exception e) {
            // Wrap and rethrow exception with a descriptive error message
            throw new Exception("Error fetching dishes: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getAvailableDishes(String reservationId, String dishType, String sort) throws Exception {
        // Step 1: Fetch reservation details from reservationTable
        Item reservation = reservationTable.getItem("reservation_id", reservationId);
        if (reservation == null) {
            throw new ReservationNotFoundException("Reservation not found for reservationId: " + reservationId);
        }

        String locationId = reservation.getString("location_id");

        // Step 2: Query dishLocationTable for dishes at the given location
        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("location_id = :v_location")
                .withValueMap(new ValueMap().withString(":v_location", locationId));

        ItemCollection<QueryOutcome> items = dishLocationTable.query(querySpec);

        // Step 3: Prepare list of dishes
        List<Map<String, Object>> availableDishes = new ArrayList<>();

        for (Item item : items) {
            String dishId = item.getString("dish_id");
            String locationSpecificAvailability = item.getString("availability");

            // Step 4: Retrieve dish details from the dish table
            GetItemSpec getItemSpec = new GetItemSpec()
                    .withPrimaryKey("id", dishId) // Specify the partition key
                    .withProjectionExpression("id, #name, price, weight, imageUrl, #state, orderCount, dishType, description")
                    .withNameMap(Map.of(
                            "#name", "name",
                            "#state", "state" // Alias for reserved word "state"
                    ));

            Item dish = dishTable.getItem(getItemSpec);
            if (dish != null) {
                // Convert the fetched item to a map
                Map<String, Object> dishDetails = dish.asMap();

                // Optionally filter by dishType
                if (dishType != null && !dishType.isEmpty()) {
                    dishType = dishType.replaceAll("[_+]", " ");
                    dishType = Arrays.stream(dishType.split("\\s+"))
                            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                            .collect(Collectors.joining(" "));

                    if (!dishType.equalsIgnoreCase((String) dishDetails.get("dishType"))) {
                        continue; // Skip this dish if it doesn't match the filter
                    }
                }

                // Step 5: Determine the final state of the dish based on global state and location-specific availability
                String globalState = (String) dishDetails.get("state");
                String finalState;

                if ("on stop".equalsIgnoreCase(globalState)) {
                    // If the global state is "on stop", it takes precedence
                    finalState = "on stop";
                } else if ("on stop".equalsIgnoreCase(locationSpecificAvailability)) {
                    // If the location-specific availability is "on stop", override global state
                    finalState = "on stop";
                } else {
                    // Otherwise, the dish is "available"
                    finalState = "available";
                }

                // Update the state in the dish details
                dishDetails.put("state", finalState);

                availableDishes.add(dishDetails);
            }
        }

        // Step 6: Sort dishes
        sortDishes(availableDishes, sort);

        return availableDishes;

    }

    private void sortDishes(List<Map<String, Object>> dishes, String sort) {
        if (sort == null) return;

        switch (sort.toLowerCase()) {
            case "popularity,asc":
                dishes.sort(Comparator.comparingInt(dish -> Integer.parseInt(dish.get("orderCount").toString())));
                break;
            case "popularity,desc":
                dishes.sort((dish1, dish2) -> Integer.parseInt(dish2.get("orderCount").toString())
                        - Integer.parseInt(dish1.get("orderCount").toString()));
                break;
            case "price,asc":
                dishes.sort(Comparator.comparing(dish -> Double.parseDouble(((String) dish.get("price")).replaceAll("[^0-9+]", ""))));
                break;
            case "price,desc":
                dishes.sort((dish1, dish2) -> Double.compare(
                        Double.parseDouble(((String) dish2.get("price")).replaceAll("[^0-9+]", "")),
                        Double.parseDouble(((String) dish1.get("price")).replaceAll("[^0-9+]", ""))));
                break;
            default:
                throw new IllegalArgumentException("Unsupported sort parameter: " + sort);
        }
    }

    // Helper function to convert DynamoDB's AttributeValue into Java's Map<String, Object>
    private Map<String, Object> convertToMap(Map<String, AttributeValue> item) {
        Map<String, Object> result = new HashMap<>();
        item.forEach((key, value) -> {
            if (value.getS() != null) {
                result.put(key, value.getS());
            } else if (value.getN() != null) {
                result.put(key, Integer.parseInt(value.getN())); // Convert Number types
            } else if (value.getBOOL() != null) {
                result.put(key, value.getBOOL());
            }
        });
        return result;
    }

    public boolean isValidDishId(String dishId) {
        // Dish ID must be at least 3 characters long and not contain special characters
        return dishId.length() > 2 && dishId.matches("^[a-zA-Z0-9-_]+$");
    }
}

