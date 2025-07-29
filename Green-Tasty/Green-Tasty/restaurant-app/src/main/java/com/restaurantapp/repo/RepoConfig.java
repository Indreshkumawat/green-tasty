package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepoConfig {

    @Value("${dish.table}")
    String dishTableName;
    @Value("${reservation.table}")
    String reservationTableName;
    @Value("${dishLocation.table}")
    String dishLocationTableName;
    @Value("${tables.table}")
    String tablesTableName;
    @Value("${customer.table}")
    String customerTableName;
    @Value("${admin.table}")
    String adminTableName;
    @Value("${waiter.table}")
    String waiterTableName;
    @Value("${location.table}")
    String locationTableName;
    @Value("${feedback.table}")
    String feedbackTableName;
    @Value("${reports.table}")
    String reportTableName;

    @Bean
    @Qualifier("customerRepo")
    CustomerRepo provideCustomerRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new CustomerRepo(dynamoDBClient, customerTableName);
    }

    @Bean
    @Qualifier("adminRepo")
    AdminRepo provideAdminRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new AdminRepo(dynamoDBClient, adminTableName);
    }

    @Bean
    @Qualifier("waiterRepo")
    WaiterRepo provideWaiterRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient,
                                 @Qualifier("locationRepo") LocationRepo locationRepo) {
        return new WaiterRepo(dynamoDBClient, locationRepo, waiterTableName);
    }

    @Bean
    @Qualifier("locationRepo")
    LocationRepo provideLocationRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient,
                                     @Qualifier("dishRepo") DishRepo dishRepo) {
        return new LocationRepo(dynamoDBClient, dishRepo, locationTableName, dishLocationTableName);
    }

    @Bean
    @Qualifier("dishRepo")
    DishRepo provideDishRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient) {
        return new DishRepo(dynamoDBClient, dishTableName, reservationTableName, dishLocationTableName);
    }

    @Bean
    @Qualifier("feedbackRepo")
    FeedbackRepo provideFeedbackRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new FeedbackRepo(dynamoDBClient, feedbackTableName);
    }

    @Bean
    @Qualifier("bookingRepo")
    BookingRepo provideBookingRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient,
                                   @Qualifier("locationRepo") LocationRepo locationRepo,
                                   @Qualifier("waiterRepo") WaiterRepo waiterRepo,
                                   @Qualifier("customerRepo") CustomerRepo customerRepo
    ) {
        return new BookingRepo(dynamoDBClient, locationRepo, waiterRepo, customerRepo, reservationTableName, tablesTableName, waiterTableName, dishLocationTableName);
    }

    @Bean
    @Qualifier("tableRepo")
    TableRepo provideTableRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient) {
        return new TableRepo(dynamoDBClient, tablesTableName);
    }

    @Bean
    @Qualifier("reportsRepo")
    ReportsRepo provideReportsRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient,
                                   @Qualifier("s3Client") AmazonS3 amazonS3Client) {
        return new ReportsRepo(dynamoDBClient, amazonS3Client, waiterTableName, reportTableName);
    }
}
