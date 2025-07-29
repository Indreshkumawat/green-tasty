package com.reportsapp.repo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DishRepo {
    private final DynamoDB dynamoDBClient;
    private final Table dishTable;

    public DishRepo(DynamoDB dynamoDBClient, String dishTableName) {
        this.dynamoDBClient = dynamoDBClient;
        this.dishTable = dynamoDBClient.getTable(dishTableName);
    }

    public Integer getDishPrice(String dishId) {
        return Integer.parseInt(dishTable
                        .getItem("id", dishId)
                        .getString("price")
                        .split("[^0-9]")[1]
        );
    }
}
