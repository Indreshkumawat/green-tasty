package com.reportsapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepoConfig {
    @Value("${reports.table}")
    String reportTableName;
    @Value("${dish.table}")
    String dishTableName;
    @Value("${feedback.table}")
    String feedbackTableName;
    @Value("${reservation.table}")
    String reservationTableName;

    @Bean("reportsRepo")
//    @Qualifier("reportsRepo")
    ReportsRepo provideReportsRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new ReportsRepo(dynamoDBClient, reportTableName);
    }

    @Bean("dishRepo")
    //@Qualifier("dishRepo")
    DishRepo provideDishRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new DishRepo(dynamoDBClient, dishTableName);
    }

    @Bean("feedbackRepo")
    //@Qualifier("feedbackRepo")
    FeedbackRepo provideFeedbackRepo(@Qualifier("dynamoDBClient") DynamoDB dynamoDBClient) {
        return new FeedbackRepo(dynamoDBClient, feedbackTableName);
    }

    @Bean("bookingRepo")
    //@Qualifier("bookingRepo")
    BookingRepo provideBookingRepo(@Qualifier("newDynamoDBClient") AmazonDynamoDB dynamoDBClient) {
        return new BookingRepo(dynamoDBClient, reservationTableName);
    }
}
