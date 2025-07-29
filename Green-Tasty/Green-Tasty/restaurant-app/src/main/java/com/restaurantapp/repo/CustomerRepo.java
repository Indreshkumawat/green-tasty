package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurantapp.dto.SignUp;
import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.exception.PasswordMismatchException;
import com.restaurantapp.exception.UserDoesNotExistsException;
import com.restaurantapp.util.ValidationUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.*;

public class CustomerRepo {
    DynamoDB dynamoDBClient;
    Table customerTable;

    public CustomerRepo(DynamoDB dynamoDBClient, String customerTableName){
        this.dynamoDBClient = dynamoDBClient;
        this.customerTable = dynamoDBClient.getTable(customerTableName);
    }

    public boolean isCustomer(String email) throws Exception {
        try {
            Item item = customerTable.getItem("email", email);
            return item != null;
        } catch (Exception e) {
            throw new Exception("Error checking entry: " + e.getMessage());
        }
    }

    public void addCustomer(SignUp signUp) throws Exception {
        try {
            Item item = new Item()
                    .withPrimaryKey("email", signUp.getEmail())
                    .withString("first_name", signUp.getFirstName())
                    .withString("last_name", signUp.getLastName())
                    .withString("password", signUp.getPassword())
                    .withString("image_url", "images/profile/default.jpg");

            customerTable.putItem(item);
        } catch (Exception e) {
            throw new Exception("Failed to add customer details: " + e.getMessage());
        }
    }

    public Map<String, Object> getCustomerDetails(String email) throws UserDoesNotExistsException {
        Item item = customerTable.getItem("email", email);
        if (item == null) throw new UserDoesNotExistsException("Customer with email " + email + " not found.");
        return item.asMap();
    }

    public List<Map<String, String>> getAllCustomers() throws RuntimeException {
        try {
            ItemCollection<ScanOutcome> items = customerTable.scan();

            List<Map<String, String>> customerList = new ArrayList<>();

            for (Item item : items) {
                customerList.add(new HashMap<String, String>(){{
                        put("name", item.getString("first_name") + " " + item.getString("last_name"));
                        put("email", item.getString("email"));
                    }}
                );
            }

            return customerList;

        } catch (Exception e) {
            throw new RuntimeException("Error fetching customers from DynamoDB");
        }
    }


    public void updateCustomerDetails(String email, String firstName, String lastName, String imageUrl) {
        String updateExpression = "SET first_name = :firstName, last_name = :lastName";
        Map<String, Object> valueMap = new HashMap<>(Map.of(
                ":firstName", firstName,
                ":lastName", lastName));

        if (imageUrl!=null && !imageUrl.isBlank()) {
            updateExpression += ", image_url = :imageUrl";
            valueMap.put(":imageUrl", imageUrl);
        }
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("email", email)
                .withUpdateExpression(updateExpression)
                .withValueMap(valueMap);
        customerTable.updateItem(updateItemSpec);
    }

    public void updateCustomerPassword(String email,UpdatePasswordRequest request) throws Exception {
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();

        Item item = customerTable.getItem("email", email);
        if (item == null) {
            throw new UserDoesNotExistsException("Customer not found with email: " + email);
        }

        String storedHashedPassword = item.getString("password");

        if (!BCrypt.checkpw(oldPassword, storedHashedPassword)) {
            throw new PasswordMismatchException("Old password does not match.");
        }
        boolean isValidPassword = ValidationUtil.isValidPassword(newPassword);

        if(!isValidPassword){
            throw new IllegalArgumentException("New password does not match with the expected criteria.");
        }

        String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("email", email)
                .withUpdateExpression("SET password = :password")
                .withValueMap(new ValueMap().withString(":password", newHashedPassword));
        customerTable.updateItem(updateItemSpec);
    }
}
