package com.reportsapp.repo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;


import java.util.Map;

public class ReportsRepo {
    private final DynamoDB dynamoDBClient;
    private final Table reportsTable;

    public ReportsRepo(DynamoDB dynamoDBClient, String reportTableName) {
        this.dynamoDBClient = dynamoDBClient;
        this.reportsTable = dynamoDBClient.getTable(reportTableName);
    }

    public void updateTable( String waiterId, String date, String updateExpression, Map<String, Object> valueMap) {
        reportsTable.updateItem(new UpdateItemSpec()
                .withPrimaryKey("report_id", waiterId + "#" + date)
                .withUpdateExpression(updateExpression)
                .withValueMap(valueMap)
        );
    }

    public void putItem(Item item) {
        reportsTable.putItem(item);
    }

    public Item getItem(String waiterId, String date) {
        return reportsTable.getItem("report_id", waiterId + "#" + date);
    }
}
