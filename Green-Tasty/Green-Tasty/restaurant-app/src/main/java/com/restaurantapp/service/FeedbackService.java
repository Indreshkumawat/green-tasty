package com.restaurantapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.restaurantapp.dto.Feedback;
import com.restaurantapp.dto.Reservation;
import com.restaurantapp.dto.ReservationStatus;
import com.restaurantapp.exception.FeedbackAlreadyExistException;
import com.restaurantapp.exception.FeedbackNotFoundException;
import com.restaurantapp.exception.ReservationAlreadyCancelledException;
import com.restaurantapp.exception.ReservationNotFoundException;
import com.restaurantapp.repo.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FeedbackService {
    private LocationRepo locationRepo;
    private BookingRepo bookingRepo;
    private FeedbackRepo feedbackRepo;
    private WaiterRepo waiterRepo;
    private AmazonSQS amazonSQSClient;
    private ReportsRepo reportsRepo;
    private AmazonS3 s3Client;
    private static final String BUCKET_NAME = "run8-team9-deployment-bucket";
    private static final String PROFILE_FOLDER = "images/profile";
    @Value("${aws.region}")
    private String awsRegion;
    @Value("${aws.account.id}")
    private String awsAccountId;
    @Value("${report_info_queue}")
    private String reportInfoQueue;

    @Autowired
    public FeedbackService(LocationRepo locationRepo, BookingRepo bookingRepo, FeedbackRepo feedbackRepo, WaiterRepo waiterRepo, AmazonSQS amazonSQSClient,ReportsRepo reportsRepo,AmazonS3 s3Client) {
        this.locationRepo = locationRepo;
        this.bookingRepo = bookingRepo;
        this.feedbackRepo = feedbackRepo;
        this.waiterRepo = waiterRepo;
        this.amazonSQSClient = amazonSQSClient;
        this.reportsRepo = reportsRepo;
        this.s3Client = s3Client;
    }

    public String postFeedbackService(Feedback feedback) throws Exception {
        String feedbackId = UUID.randomUUID().toString();

        if (feedback.getReservationId() == null || feedback.getReservationId().isEmpty()) {
            throw new IllegalArgumentException("reservationId is required.");
        }

        Reservation reservation = bookingRepo.getReservationById(feedback.getReservationId());
        if (bookingRepo.feedbackExistsByReservationId(feedback.getReservationId())) {
            throw new FeedbackAlreadyExistException("Feedback already exists for the reservation.");
        }


        if (Float.isNaN(feedback.getCuisineRating()) && Float.isNaN(feedback.getServiceRating())) {
            throw new IllegalArgumentException("Both cuisineRating and serviceRating are required.");
        }

        if (Float.isNaN(feedback.getCuisineRating()) && !feedback.getCuisineComment().isEmpty() && !Float.isNaN(feedback.getServiceRating())) {
            throw new IllegalArgumentException("Missing cuisineRating.");
        }

        if (Float.isNaN(feedback.getServiceRating()) && !feedback.getServiceComment().isEmpty() && !Float.isNaN(feedback.getCuisineRating())) {
            throw new IllegalArgumentException("Missing serviceRating.");
        }

        Float serviceRating = 0.0f;
        if (!Float.isNaN(feedback.getServiceRating())) {
            try {
                serviceRating = feedback.getServiceRating();
                if (serviceRating < 1 || serviceRating > 5) {
                    throw new IllegalArgumentException("serviceRating must be between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException( "Invalid serviceRating value.");
            }
        }

        Float cuisineRating = 0.0f;
        if (!Float.isNaN(feedback.getCuisineRating())) {
            try {
                cuisineRating = feedback.getCuisineRating();
                if (cuisineRating < 1 || cuisineRating> 5) {
                    throw new IllegalArgumentException("CuisineRating must be between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException( "Invalid cuisineRating value.");
            }
        }

        // Set feedback ID
        Map<String,Object> feedbackToSave = Map.of(
                "feedbackId",feedbackId,
                "reservationId",feedback.getReservationId(),
                "cuisineComment",feedback.getCuisineComment(),
                "cuisineRating",cuisineRating,
                "serviceComment",feedback.getServiceComment(),
                "serviceRating",serviceRating,
                "locationId",reservation.getLocationId());

        // Save feedback
        feedbackRepo.createNewFeedback(feedbackToSave);

        // Update reservation
        bookingRepo.updateReservationFeedback(feedback.getReservationId(), feedbackId);
        bookingRepo.updateReservationStatus(feedback.getReservationId(), ReservationStatus.FINISHED.toString());

        // Update waiter and location ratings
        String waiterEmail = bookingRepo.getWaiterEmailByReservationId(feedback.getReservationId());
        waiterRepo.updateWaiterAverageRatings(waiterEmail, serviceRating);

        String locationId = reservation.getLocationId();
        locationRepo.updateLocationAverageRatings(locationId, cuisineRating);

        // Send SQS Message
        SendMessageRequest sendMessageRequest = new SendMessageRequest()
                .withQueueUrl("https://sqs." + awsRegion + ".amazonaws.com/" + awsAccountId + "/" + reportInfoQueue)
                .withMessageBody(new JSONObject()
                        .put("date", reservation.getDate())
                        .put("reservation_id", feedback.getReservationId())
                        .put("waiter_email", waiterEmail)
                        .put("feedback_id", feedbackId)
                        .put("pre_order", reservation.getPreOrder())
                        .put("table_id", reservation.getTableIds())
                        .put("location_id", reservation.getLocationId())
                        .put("status", reservation.getStatus())
                        .put("time_slot", reservation.getTimeSlot())
                        .put("guests_number", reservation.getGuestsNumber())
                        .toString());

        amazonSQSClient.sendMessage(sendMessageRequest);


        return "Feedback has been created";
    }


    public String putFeedbackService(Feedback feedback) throws Exception{
        if (feedback.getReservationId() == null || feedback.getReservationId().isEmpty()) {
            throw new IllegalArgumentException("reservationId is required.");
        }
        Reservation reservation = bookingRepo.getReservationById(feedback.getReservationId());


        System.out.println(feedback.toString());
        if (feedback.getCuisineRating() == 0 && feedback.getServiceRating() == 0) {
            throw new IllegalArgumentException("Both cuisineRating and serviceRating are required.");
        }

        if (feedback.getCuisineRating() == 0 && !feedback.getCuisineComment().isEmpty() && feedback.getServiceRating()!=0) {
            throw new IllegalArgumentException("Missing cuisineRating.");
        }
        if (feedback.getServiceRating() == 0 && !feedback.getServiceComment().isEmpty() && feedback.getCuisineRating()!=0) {
            throw new IllegalArgumentException("Missing serviceRating.");
        }

        String feedbackId = reservation.getFeedbackId();
        if (feedbackId == null || feedbackId.trim().isEmpty()) {
            throw new FeedbackNotFoundException ("Feedback not found for the provided reservation.");
        }

        // Retrieve the old feedback entry
        Feedback oldFeedback = feedbackRepo.getFeedbackById(feedbackId);


        Float newServiceRating = 0.0f;
        if (!Float.isNaN(feedback.getServiceRating())) {
            try {
                newServiceRating = feedback.getServiceRating();
                if (newServiceRating < 1 || newServiceRating > 5) {
                    throw new IllegalArgumentException("serviceRating must be between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException( "Invalid serviceRating value.");
            }
        }

        Float newCuisineRating = 0.0f;
        if (!Float.isNaN(feedback.getCuisineRating())) {
            try {
                newCuisineRating = feedback.getCuisineRating();
                if (newCuisineRating < 1 || newCuisineRating> 5) {
                    throw new IllegalArgumentException("CuisineRating must be between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException( "Invalid cuisineRating value.");
            }
        }

        Map<String,Object> updatedFeedback = Map.of(
                "feedbackId",feedbackId,
                "reservationId",feedback.getReservationId(),
                "cuisineComment",feedback.getCuisineComment(),
                "newCuisineRating",newCuisineRating != 0 ? newCuisineRating : oldFeedback.getCuisineRating(),
                "serviceComment",feedback.getServiceComment(),
                "newServiceRating",newServiceRating != 0 ? newServiceRating : oldFeedback.getServiceRating(),
                "locationId",reservation.getLocationId());


        feedbackRepo.updateFeedback(updatedFeedback);

        String waiterEmail = bookingRepo.getWaiterEmailByReservationId(feedback.getReservationId());
        // Adjust the waiter ratings if `serviceRating` is updated
        if (!newServiceRating.equals(oldFeedback.getServiceRating())) {
            waiterRepo.adjustWaiterServiceRating(waiterEmail, oldFeedback.getServiceRating(), newServiceRating);
        }

        // Adjust the location ratings if `cuisineRating` is updated
        if (!newCuisineRating.equals(oldFeedback.getCuisineRating())) {
            String locationId = reservation.getLocationId();
            locationRepo.adjustLocationRating(locationId, oldFeedback.getCuisineRating(), newCuisineRating);
        }

        // Update reports table with both new ratings
        BigDecimal oldServiceRatingDecimal = BigDecimal.valueOf(oldFeedback.getServiceRating());
        BigDecimal newServiceRatingDecimal = BigDecimal.valueOf(newServiceRating);
        BigDecimal oldCuisineRatingDecimal = BigDecimal.valueOf(oldFeedback.getCuisineRating());
        BigDecimal newCuisineRatingDecimal = BigDecimal.valueOf(newCuisineRating);

        reportsRepo.updateReportWithUpdatedFeedback(
                waiterEmail,
                reservation.getDate(),
                oldServiceRatingDecimal,
                newServiceRatingDecimal,
                oldCuisineRatingDecimal,
                newCuisineRatingDecimal
        );

        return "Feedback has been updated";


    }

    public Map<String,Object> getWaiterDetailsService(Map<String,String> allParams) throws Exception{
        Feedback feedback;
        String cuisineComment = "";
        String serviceComment = "";
        String waiterEmail = allParams.getOrDefault("waiterEmail","");
        String feedbackId = allParams.getOrDefault("feedbackId",null);
        Map<String, Object> waiter = waiterRepo.getWaiterDetails(waiterEmail);
        String fileName = (String)waiter.get("image_url");
        if(feedbackId!=null){
            feedback = feedbackRepo.getFeedbackById(feedbackId);
            cuisineComment = feedback.getCuisineComment();
            serviceComment = feedback.getServiceComment();
        }

        String waiterName = (String)waiter.getOrDefault("first_name","DefaultWaiter")+" "+ (String)waiter.getOrDefault("last_name","DefaultWaiter");
        Object imageUrl =  s3Client.generatePresignedUrl(new GeneratePresignedUrlRequest(BUCKET_NAME, fileName)
                .withExpiration(new Date(System.currentTimeMillis() + (5L * 60 * 1000))));
        return Map.of("waiterName",waiterName,
                "waiterRating",waiter.get("waiter_rating"),
                "imageUrl",imageUrl,
                "cuisineComment",cuisineComment,
                "serviceComment",serviceComment);
    }


}
