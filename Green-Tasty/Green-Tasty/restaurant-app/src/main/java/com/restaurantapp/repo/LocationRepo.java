package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurantapp.dto.Dish;
import com.restaurantapp.dto.Location;
import com.restaurantapp.exception.LocationNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LocationRepo {
    DynamoDB dynamoDBClient;
    DishRepo dishRepo;
    Table locationTable;
    Table dishLocationTable;

    @Autowired
    public LocationRepo(
            DynamoDB dynamoDBClient,
            DishRepo dishRepo,
            String locationTableName,
            String dishLocationTableName
    ) {
        this.dynamoDBClient = dynamoDBClient;
        this.dishRepo = dishRepo;
        this.locationTable = dynamoDBClient.getTable(locationTableName);
        this.dishLocationTable = dynamoDBClient.getTable(dishLocationTableName);
    }

    public List<Location> getAllLocations(){
        try {
            ItemCollection<ScanOutcome> items = locationTable.scan();
            List<Location> locations = new ArrayList<>();
            for (Item item : items) {
                locations.add(Location.fromMap(item.asMap()));
            }
            return locations;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching locations from DynamoDb.");
        }
    }

    public Location getLocationById(String id) throws LocationNotFoundException {
        try {
            Item item = locationTable.getItem("location_id", id);
            System.out.println(item);
            if (item == null) {
                throw new LocationNotFoundException("Location Id "+id+" not found");
            }
            return Location.fromMap(item.asMap());
        } catch (RuntimeException e) {
            throw new RuntimeException("Error fetching location for locationId: " + id);
        }
    }

    public List<Map<String,Object>> getSpecialDishesForLocation(String locationId){
        try {
            // Query the DishLocation table for the given locationId
            QuerySpec querySpec = new QuerySpec()
                    .withKeyConditionExpression("location_id = :v_location")
                    .withFilterExpression("isSpecial = :v_special")
                    .withValueMap(new ValueMap()
                            .withString(":v_location", locationId)
                            .withBoolean(":v_special", true));

            ItemCollection<QueryOutcome> items = dishLocationTable.query(querySpec);
            List<Map<String,Object>> specialDishes = new ArrayList<>();

            // Fetch detailed dish information from the Dish table using dishId
            for (Item item : items) {
                String dishId = item.getString("dish_id");

                // Fetch dish details from Dish table
                 Map<String,Object> dish= dishRepo.getDishByIdForLocation(dishId);
                specialDishes.add(dish);
            }

            return specialDishes;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching special dishes for locationId: " + locationId);
        }
    }



    // getting location info for drop down
    public List<Map<String,Object>> getLocationInfoOptions(){
        ItemCollection<ScanOutcome> items = locationTable.scan();
        List<Map<String,Object>> locationInfoArray = new ArrayList<>();
        for(Item location : items){
            Map<String, Object> locationMap = new HashMap<>();
            locationMap.put("locationId",location.getString("location_id"));
            locationMap.put("address",location.getString("address"));
            locationInfoArray.add(locationMap);
        }
        return locationInfoArray;
    }


    public String getLocationAddress(String locationId) throws Exception {
       try{
           Item item = locationTable.getItem("location_id", locationId);
           if(item==null)return null;
           return item.getString("address");
       }
       catch(Exception e){
           throw new Exception("Error fetching location address from DynamoDB. "+e.getMessage());
       }

    }

    public void updateLocationAverageRatings(String locationId, float newLocationRating) throws Exception {
        try {

            Item locationItem = locationTable.getItem("location_id", locationId);

            if (locationItem == null) {
                throw new IllegalArgumentException("Location not found with ID: " + locationId);
            }

            float currentAverageRating = Float.parseFloat(locationItem.getString("rating"));
            float totalRatingsCount = locationItem.getFloat("total_rating_count");

            float rawNewAverage = (currentAverageRating * totalRatingsCount + newLocationRating) / (totalRatingsCount + 1);

            // Use BigDecimal for precise rounding to 2 decimal places
            BigDecimal updatedAverageRatingString = new BigDecimal(rawNewAverage)
                    .setScale(2, RoundingMode.HALF_UP);

            locationTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("location_id", locationId)
                    .withUpdateExpression("SET rating = :avg, total_rating_count = :count")
                    .withValueMap(new ValueMap()
                            .withString(":avg", String.valueOf(updatedAverageRatingString))
                            .withNumber(":count", totalRatingsCount + 1))
            );

            System.out.println("Location ratings updated successfully. Location ID: " +locationId);

        } catch (Exception e) {
            throw new Exception("Error updating Location ratings: " + e.getMessage(), e);
        }
    }
    public void adjustLocationRating(String locationId, float oldLocationRating, float newLocationRating) throws Exception {
        try {
            Item locationItem = locationTable.getItem("location_id", locationId);

            if (locationItem == null) {
                throw new LocationNotFoundException("Location not found with ID: " + locationId);
            }
            float currentAverageLocationRating = Float.parseFloat(locationItem.getString("rating"));
            float totalRatingsCount = locationItem.getFloat("total_rating_count");
            float ratingDifference = newLocationRating - oldLocationRating;
            float rawAdjustedAverage = currentAverageLocationRating + (ratingDifference / totalRatingsCount);

            // Round to 2 decimal places using BigDecimal
            BigDecimal adjustedAverage = new BigDecimal(rawAdjustedAverage)
                    .setScale(2, RoundingMode.HALF_UP);
            String adjustedAverageString = String.valueOf(adjustedAverage);
            locationTable.updateItem(new UpdateItemSpec()
                    .withPrimaryKey("location_id", locationId)
                    .withUpdateExpression("SET rating = :newAvg")
                    .withValueMap(new ValueMap()
                            .withString(":newAvg", adjustedAverageString))
            );

        } catch (Exception e) {
            throw new Exception("Error updating Location ratings: " + e.getMessage(), e);
        }
    }
}
