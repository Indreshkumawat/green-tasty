package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.restaurantapp.dto.SignUp;
import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.exception.AdminCheckException;
import com.restaurantapp.exception.PasswordMismatchException;
import com.restaurantapp.exception.UserDoesNotExistsException;
import com.restaurantapp.util.ValidationUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

public class AdminRepo {
    DynamoDB dynamoDBClient;
    Table adminTable;

    public AdminRepo(DynamoDB dynamoDBClient, String adminTable) {
        this.dynamoDBClient = dynamoDBClient;
        this.adminTable = dynamoDBClient.getTable(adminTable);
    }

    public boolean isAdmin(String email) throws AdminCheckException {
        try {
            Item item = adminTable.getItem("email", email);
            return item != null;
        } catch (Exception e) {
            throw new AdminCheckException("Error checking entry: " + e.getMessage());
        }
    }

    public void addAdmin(SignUp signUp) throws AdminCheckException {
        try {
            Item item = new Item()
                    .withPrimaryKey("email", signUp.getEmail())
                    .withString("first_name", signUp.getFirstName())
                    .withString("last_name", signUp.getLastName())
                    .withString("password", signUp.getPassword())
                    .withString("image_url", "images/profile/default.jpg");

            adminTable.putItem(item);
        } catch (Exception e) {
            throw new AdminCheckException("Failed to add admin details: " + e.getMessage());
        }
    }

    public Map<String, Object> getAdminDetails(String email) {
        Item item = adminTable.getItem("email", email);
        return item.asMap();
    }

    public void updateAdminInfo(String email, String firstName, String lastName, String imageUrl) throws AdminCheckException {
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
            UpdateItemOutcome outcome = adminTable.updateItem(
                    "email", email, // Primary key (partition key)
                    "SET #first_name = :first_name, #last_name = :last_name" + addToExpression, // Update expression
                    nameMap,
                    valueMap
            );


        } catch (Exception e) {
            throw new AdminCheckException("Failed to update admin details: " + e.getMessage());
        }

    }

    public void updateAdminPassword(String email, UpdatePasswordRequest request) throws UserDoesNotExistsException, PasswordMismatchException, IllegalArgumentException {
        String oldPassword = request.oldPassword();
        String newPassword = request.newPassword();

        Item item = adminTable.getItem("email", email);
        if (item == null) {
            throw new UserDoesNotExistsException("Admin not found with email: " + email);
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

        adminTable.updateItem(updateItemSpec);
    }
}
