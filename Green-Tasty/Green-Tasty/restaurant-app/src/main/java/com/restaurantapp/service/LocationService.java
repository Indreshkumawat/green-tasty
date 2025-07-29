package com.restaurantapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.restaurantapp.dto.Feedback;
import com.restaurantapp.dto.Location;
import com.restaurantapp.dto.Reservation;
import com.restaurantapp.exception.LocationNotFoundException;
import com.restaurantapp.exception.ReservationAlreadyCancelledException;
import com.restaurantapp.exception.ReservationNotFoundException;
import com.restaurantapp.exception.UserDoesNotExistsException;
import com.restaurantapp.repo.BookingRepo;
import com.restaurantapp.repo.CustomerRepo;
import com.restaurantapp.repo.FeedbackRepo;
import com.restaurantapp.repo.LocationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class LocationService {

    private LocationRepo locationRepo;
    private BookingRepo bookingRepo;
    private FeedbackRepo feedbackRepo;
    private CustomerRepo customerRepo;
    private final Logger logger;
    private AmazonS3 s3Client;

    private static final String BUCKET_NAME = "run8-team9-deployment-bucket";

    @Autowired
    public LocationService(LocationRepo locationRepo, BookingRepo bookingRepo, FeedbackRepo feedbackRepo, CustomerRepo customerRepo, AmazonS3 s3Client) {
        this.locationRepo = locationRepo;
        this.bookingRepo = bookingRepo;
        this.feedbackRepo = feedbackRepo;
        this.customerRepo = customerRepo;
        this.s3Client = s3Client;
        logger = LoggerFactory.getLogger(TableService.class);
    }

    public List<Location> getAllLocationsService() {
            return locationRepo.getAllLocations();
    }

    public List<Map<String,Object>> getSpecialityDishesByLocationService(String locationId) throws LocationNotFoundException {
            if (locationId == null || locationId.isBlank()) {
                throw new IllegalArgumentException("Missing or Invalid Id");
            }
            Location location = locationRepo.getLocationById(locationId);
            return locationRepo.getSpecialDishesForLocation(locationId);
    }

    public  Map<String, Object> getFeedbacksByLocationService(String locationId, Map<String, String> allParams) throws IllegalArgumentException, LocationNotFoundException, UserDoesNotExistsException, ReservationNotFoundException, ReservationAlreadyCancelledException {
            if (locationId == null || locationId.isBlank()) {
                throw new IllegalArgumentException("Missing or Invalid Id");
            }
           // Location location = locationRepo.getLocationById(locationId);

            Map<String, Object> queryParams = extractQueryParameters(allParams);
            if ((Integer) queryParams.get("size") > 20) {
                throw new IllegalArgumentException("Size limit exceeded");
            }

           // List<Map<String, String>> reservations = bookingRepo.getReservationsByLocation(locationId);
            List<Map<String, Object>> allFeedbacks = buildFeedbackList((String) queryParams.get("type"), locationId);

            List<Map<String, Object>> sortedPaginated = sortAndPaginate(
                    allFeedbacks,
                    (String) queryParams.get("sortKey"),
                    (Boolean) queryParams.get("ascending"),
                    (Integer) queryParams.get("page"),
                    (Integer) queryParams.get("size")
            );

            return buildResponse(allFeedbacks, sortedPaginated, queryParams);

    }

    public Map<String, Object> extractQueryParameters( Map<String, String> requestParams) {

        String type = requestParams.getOrDefault("type", "");
        if(type.isEmpty()) throw new IllegalArgumentException("Type is not provided");
        int page = parseOrDefault(requestParams.get("page"), 0);
        int size = parseOrDefault(requestParams.get("size"), 1);
        String sortParam = requestParams.getOrDefault("sort", "date,asc");
        String[] sortParts = sortParam.split(",");
        String sortKey = sortParts[0];
        boolean ascending = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]);

        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("page", page);
        result.put("size", size);
        result.put("sortKey", sortKey);
        result.put("ascending", ascending);

        return result;

    }
    private int parseOrDefault(String value,int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private List<Map<String, Object>> buildFeedbackList(String type, String locationId) throws UserDoesNotExistsException, ReservationNotFoundException, ReservationAlreadyCancelledException {
        List<Map<String, Object>> feedbackList = new ArrayList<>();
        List<Map<String,Object>> feedbacks = feedbackRepo.getFeedbacksByLocation(locationId);
        for (Map<String,Object> feedback: feedbacks){
            Map<String, Object> feedbackMap = new HashMap<>();
            feedbackMap.put("id",feedback.get("feedback_id"));
            if ("service".equalsIgnoreCase(type)) {
                feedbackMap.put("rate", feedback.getOrDefault("service_rating", ""));
                feedbackMap.put("comment", feedback.getOrDefault("service_comment", ""));
                feedbackMap.put("type", "Service");
            } else if ("cuisine".equalsIgnoreCase(type)) {
                feedbackMap.put("rate", feedback.getOrDefault("cuisine_rating", ""));
                feedbackMap.put("comment", feedback.getOrDefault("cuisine_comment", ""));
                feedbackMap.put("type", "Cuisine");
            } else {
                throw new IllegalArgumentException("Invalid feedback type!");
            }
            Map<String,Object> reservation = bookingRepo.getReservation((String)feedback.get("reservation_id"));
            feedbackMap.put("date",reservation.get("date"));
            Map<String, Object> customerDetails = customerRepo.getCustomerDetails((String)reservation.getOrDefault("customer_email","visitor"));
            String fileName = (String) customerDetails.getOrDefault("image_url", "");
            String userName = customerDetails.getOrDefault("first_name", "") + " " + customerDetails.getOrDefault("last_name", "");
            feedbackMap.put("userName", userName.trim());
            feedbackMap.put("userAvatarUrl", s3Client.generatePresignedUrl(new GeneratePresignedUrlRequest(BUCKET_NAME, fileName).withExpiration(new Date(System.currentTimeMillis() + (5L * 60 * 1000)))));
            feedbackMap.put("locationId",locationId);
            feedbackList.add(feedbackMap);
        }

        return feedbackList;
    }

    public List<Map<String, Object>> sortAndPaginate(
            List<Map<String, Object>> feedbacks,
            String sortKey,
            boolean ascending,
            int page,
            int size) {


        if (sortKey.equals("date")) {
            // Sort by "date"
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            feedbacks.sort((r1, r2) -> {
                try {
                    Date date1 = dateFormat.parse((String) r1.get("date"));
                    Date date2 = dateFormat.parse((String) r2.get("date"));
                    return ascending ? date1.compareTo(date2) : date2.compareTo(date1);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid date format in review data");
                }
            });
        } else if (sortKey.equals("rate")) {
            // Sort by "rate"
            feedbacks.sort((r1, r2) -> {
                BigDecimal rate1 = (BigDecimal) r1.get("rate");
                BigDecimal rate2 = (BigDecimal) r2.get("rate");
                return ascending ? rate1.compareTo(rate2) : rate2.compareTo(rate1);
            });
        } else {
            throw new IllegalArgumentException("Invalid sortKey. Use 'date' or 'rate'.");
        }


        // Pagination logic
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, feedbacks.size());

        // Check bounds for pagination
        if (fromIndex >= feedbacks.size()) {
            return new ArrayList<>(); // Return an empty list if the page is out of bounds
        }

        return feedbacks.subList(fromIndex, toIndex);
    }


    public  Map<String, Object> buildResponse(
            List<Map<String, Object>> allFeedbacks,
            List<Map<String, Object>> sortedPaginated,
            Map<String, Object> queryParams) {

        Map<String, Object> response = new LinkedHashMap<>();

        int size = (Integer) queryParams.get("size");
        int page = (Integer) queryParams.get("page");
        boolean ascending = (Boolean) queryParams.get("ascending");
        String sortKey = (String) queryParams.get("sortKey");

        // Sorting metadata
        Map<String, Object> sortInfo = new LinkedHashMap<>();
        sortInfo.put("direction", ascending ? "asc" : "desc");
        sortInfo.put("nullHandling", "NATIVE");
        sortInfo.put("ascending", ascending);
        sortInfo.put("property", sortKey);
        sortInfo.put("ignoreCase", true);

        List<Map<String, Object>> sortList = Collections.singletonList(sortInfo);

        // Pageable metadata
        Map<String, Object> pageable = new LinkedHashMap<>();
        pageable.put("paged", true);
        pageable.put("unpaged", false);
        pageable.put("pageSize", size);
        pageable.put("pageNumber", page);
        pageable.put("offset", page * size);
        pageable.put("sort", sortList);

        // Pagination core fields
        response.put("content", sortedPaginated);
        response.put("totalElements", allFeedbacks.size());
        response.put("totalPages", (int) Math.ceil((double) allFeedbacks.size() / size));
        response.put("size", size);
        response.put("number", page);
        response.put("numberOfElements", sortedPaginated.size());
        response.put("first", page == 0);
        response.put("last", page == (allFeedbacks.size() - 1) / size);
        response.put("empty", sortedPaginated.isEmpty());

        // Metadata
        response.put("sort", sortList);
        response.put("pageable", pageable);

        return response;
    }

    public List<Map<String,Object>> getLocationInfoOptionsService(){
        return locationRepo.getLocationInfoOptions();
    }

    public Location getLocationByIdService(String locationId) throws LocationNotFoundException {
        return locationRepo.getLocationById(locationId);
    }




}
