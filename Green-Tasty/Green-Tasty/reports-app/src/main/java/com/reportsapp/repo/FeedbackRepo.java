package com.reportsapp.repo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;

public class FeedbackRepo {
    DynamoDB dynamoDBClient;
    Table feedbackTable;

    public FeedbackRepo(DynamoDB dynamoDBClient, String feedbackTableName) {
        this.dynamoDBClient = dynamoDBClient;
        this.feedbackTable = dynamoDBClient.getTable(feedbackTableName);
    }

    public Item getItem(String feedbackId) {
        return feedbackTable.getItem("feedback_id", feedbackId);
    }
}
