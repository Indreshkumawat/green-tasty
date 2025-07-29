package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.xspec.N;
import com.restaurantapp.dto.Feedback;
import com.restaurantapp.exception.FeedbackNotFoundException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackRepo {
    DynamoDB dynamoDBClient;
    Table feedbackTable;

    @Autowired
    public FeedbackRepo(DynamoDB dynamoDBClient,String feedbackTableName) {
        this.dynamoDBClient = dynamoDBClient;
        this.feedbackTable = dynamoDBClient.getTable(feedbackTableName);
    }

    public void createNewFeedback(Map<String,Object>feedback) throws Exception {
        try {
            feedbackTable.putItem(new Item()
                    .withPrimaryKey("feedback_id", feedback.get("feedbackId"))
                    .withString("reservation_id", (String)feedback.get("reservationId"))
                    .withString("cuisine_comment", (String)feedback.get("cuisineComment"))
                    .withString("service_comment", (String)feedback.get("serviceComment"))
                    .withNumber("cuisine_rating", (Number) feedback.get("cuisineRating"))
                    .withNumber("service_rating", (Number)feedback.get("serviceRating"))
                    .withString("location_id",(String)feedback.get("locationId"))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update feedback details: " + e.getMessage());
        }
    }


    public JSONArray sortAndPaginate(JSONArray feedbacks, String sortKey, boolean ascending, int page, int size) {
        // Convert JSONArray to List for sorting
        List<JSONObject> feedbackList = new ArrayList<>();
        feedbacks.forEach(obj -> feedbackList.add((JSONObject) obj));
        // Sorting logic
        feedbackList.sort((f1, f2) -> {
            if ("date".equalsIgnoreCase(sortKey)) {
                // Sort by the `date` field (string comparison of date values)
                return ascending ? f1.getString("date").compareTo(f2.getString("date"))
                        : f2.getString("date").compareTo(f1.getString("date"));
            } else if ("rate".equalsIgnoreCase(sortKey)) {
                // Sort by the `rate` field (lexicographic string comparison)
                return ascending ? f1.getString("rate").compareTo(f2.getString("rate"))
                        : f2.getString("rate").compareTo(f1.getString("rate"));
            }
            return 0; // No sorting if invalid `sortKey` is provided
        });

        // Pagination logic
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, feedbackList.size());
        List<JSONObject> paginatedFeedbacks = (fromIndex > feedbackList.size()) ? new ArrayList<>() : feedbackList.subList(fromIndex, toIndex);
        return new JSONArray(paginatedFeedbacks); // Convert back to JSONArray
    }

    public void updateFeedback (Map<String,Object>feedback){
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("feedback_id",feedback.get("feedbackId"))
                .withUpdateExpression("set cuisine_comment =:cc,cuisine_rating = :cr,service_comment = :sc,service_rating = :sr")
                .withValueMap(new ValueMap()
                        .withString(":cc",(String)feedback.get("cuisineComment"))
                        .withNumber(":cr",(Number) feedback.get("newCuisineRating"))
                        .withString(":sc", (String)feedback.get("serviceComment"))
                        .withNumber(":sr",(Number)feedback.get("newServiceRating")))
                .withReturnValues(ReturnValue.UPDATED_NEW);
        feedbackTable.updateItem(updateItemSpec);
    }


    public Feedback getFeedbackById(String feedbackId) throws Exception {
        try {
            Item feedbackItem = feedbackTable.getItem("feedback_id", feedbackId);

            if (feedbackItem == null) {
                throw new FeedbackNotFoundException("Feedback not found with ID: " + feedbackId);
            }

            return new Feedback.FeedbackBuilder()
                    .setFeedbackId(feedbackItem.getString("feedback_id"))
                    .setReservationId(feedbackItem.getString("reservation_id"))
                    .setServiceRating(feedbackItem.getFloat("service_rating"))
                    .setServiceComment(feedbackItem.getString("service_comment"))
                    .setCuisineRating(feedbackItem.getFloat("cuisine_rating"))
                    .setCuisineComment(feedbackItem.getString("cuisine_comment"))
                    .createFeedback();

        } catch (Exception e) {
            throw new Exception("Error retrieving feedback with ID: " + feedbackId + ". " + e.getMessage(), e);
        }
    }

    public List<Map<String,Object>> getFeedbacksByLocation(String locationId){
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        Index locationIndex = feedbackTable.getIndex("location_id-index");

        QuerySpec querySpec = new QuerySpec()
                .withKeyConditionExpression("location_id = :v_location")
                .withValueMap(new ValueMap().withString(":v_location", locationId));

        ItemCollection<QueryOutcome> items = locationIndex.query(querySpec);

        for (Item item : items) {
            feedbackList.add(item.asMap());
        }
        return feedbackList;
    }

}
