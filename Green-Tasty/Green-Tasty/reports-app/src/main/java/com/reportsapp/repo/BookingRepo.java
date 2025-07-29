package com.reportsapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;

import java.util.List;
import java.util.Map;

public class BookingRepo {
    private final AmazonDynamoDB amazonDynamoDBClient;
    private final String reservationTableName;

    public BookingRepo(AmazonDynamoDB amazonDynamoDBClient, String reservationTableName) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.reservationTableName = reservationTableName;
    }

    public List<Map<String, AttributeValue>> getItems(String waiterId, String timeSlot) {
        QueryRequest queryRequest = new QueryRequest()
                .withTableName(reservationTableName)
                .withIndexName("waiter_email-time_slot-index")
                .withKeyConditionExpression("waiter_email = :waiter_id AND time_slot = :time_slot")
                .withExpressionAttributeValues(Map.of(":waiter_id", new AttributeValue().withS(waiterId),
                        ":time_slot", new AttributeValue().withS(timeSlot))
                );
        return amazonDynamoDBClient.query(queryRequest).getItems();
    }
}
