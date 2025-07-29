package com.restaurantapp.service;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.restaurantapp.dto.Location;
import com.restaurantapp.dto.Reservation;
import com.restaurantapp.dto.WaiterReport;
import com.restaurantapp.exception.ValidationException;
import com.restaurantapp.dto.Tables;
import com.restaurantapp.exception.*;
import com.amazonaws.services.kms.model.NotFoundException;
import com.restaurantapp.exception.LocationNotFoundException;
import com.restaurantapp.exception.TableAlreadyReservedException;
import com.restaurantapp.exception.TimeSlotNotFoundException;
import com.restaurantapp.repo.*;
import com.restaurantapp.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    private BookingRepo bookingRepo;
    private TableRepo tableRepo;
    private LocationRepo locationRepo;
    private CustomerRepo customerRepo;
    private WaiterRepo waiterRepo;
    private final Logger logger;

    @Autowired
    public BookingService(BookingRepo bookingRepo, TableRepo tableRepo, LocationRepo locationRepo, CustomerRepo customerRepo, WaiterRepo waiterRepo) {
        this.bookingRepo = bookingRepo;
        this.tableRepo = tableRepo;
        this.locationRepo = locationRepo;
        this.customerRepo = customerRepo;
        this.waiterRepo = waiterRepo;
        logger = LoggerFactory.getLogger(BookingService.class);
    }

    public List<Map<String, Object>> getAllReservations(String userEmail, String date, String time, String tableNumber) throws Exception {
        return bookingRepo.getAllReservation(userEmail, date, time, tableNumber);
    }

    public void addDishToCartForReservation(String reservationId, String dishId) throws Exception {

        String validateReservationId = ValidationUtil.isValidString(reservationId, "RESERVATION-ID");
        if (validateReservationId != null)
            throw new ValidationException(validateReservationId);
        String validateDishId = ValidationUtil.isValidString(dishId, "DISH-ID");

        if (validateDishId != null)
            throw new ValidationException(validateDishId);

        Reservation reservationDetails = bookingRepo.getReservationById(reservationId);
        Location locationDetails = locationRepo.getLocationById(reservationDetails.getLocationId());
        bookingRepo.addDishToCartForReservation(reservationId, dishId, LocalDateTime.now(ZoneId.of(locationDetails.getZone())));
    }

    private boolean checkIfSameTableNeedsToBeAllotted(String requestedTimeSlot, String reservedTimeSlot, int requestedGuestNum, int reservedGuestNum) {
        if (requestedTimeSlot.equals(reservedTimeSlot)) {
            return (reservedGuestNum % 2 == 0 && (reservedGuestNum - 1) == requestedGuestNum) ||
                    (reservedGuestNum % 2 != 0 && (reservedGuestNum + 1) == requestedGuestNum);
        } else
            return false;
    }

    private boolean checkIfSubmittedReservationEqualsExistingReservation(String requestedTimeSlot, String reservedTimeSlot, int requestedGuestNum, int reservedGuestNum) {
        return (requestedTimeSlot.equals(reservedTimeSlot) && requestedGuestNum == reservedGuestNum);
    }

    public Map<String,String> editReservation(Map<String,String> requestBody, String reservationId) throws Exception
    {
        String guestsNum = requestBody.getOrDefault("guestsNumber", "0");
        String timeFrom = requestBody.getOrDefault("timeFrom", "");
        String timeTo = requestBody.getOrDefault("timeTo", "");
        String timeSlot = timeFrom + "-" + timeTo;

        int guestsNumber = Integer.parseInt(guestsNum);

        //validation of the request body.
        String validationErrorGuestNumber = ValidationUtil.isValidGuestNumber(guestsNumber);
        if (validationErrorGuestNumber != null){
            throw new ValidationException(validationErrorGuestNumber);

        }

        String validationErrorTimeSlot = ValidationUtil.isValidTimeSlot(timeFrom, timeTo);
        if (validationErrorTimeSlot != null)
            throw new ValidationException(validationErrorTimeSlot);


        if (!bookingRepo.isReserved(reservationId)) {
            throw new ReservationAlreadyCancelledException("Reservation with ID " + reservationId + " already cancelled.");
        }
        Reservation reservationDetails = bookingRepo.getReservationById(reservationId);
        List<String> bookedTables = reservationDetails.getTableIds();
        Location locationDetails = locationRepo.getLocationById(reservationDetails.getLocationId());

        //submitted reservation equals existing reservation
        if (checkIfSubmittedReservationEqualsExistingReservation(timeSlot, reservationDetails.getTimeSlot(), guestsNumber, reservationDetails.getGuestsNumber())) {
            return Map.of("message", "No changes were made to the reservation.");
        }
        //condition when the table will be same just the guestNumber needs to be modified in reservation table
        else if (checkIfSameTableNeedsToBeAllotted(timeSlot, reservationDetails.getTimeSlot(), guestsNumber, reservationDetails.getGuestsNumber())) {
            bookingRepo.editReservation(null, reservationId, null, guestsNumber, LocalDateTime.now(ZoneId.of(locationDetails.getZone())));
        } else {
            List<Tables> availableTables = tableRepo.getAvailableTables(reservationDetails.getLocationId(),
                    reservationDetails.getDate(), timeFrom,
                    guestsNum, locationDetails.getZone());


            bookingRepo.editReservation(availableTables, reservationId, timeSlot, guestsNumber, LocalDateTime.now(ZoneId.of(locationDetails.getZone())));

            //freeing the previously booked tables
            logger.info("............Going to free the booked tables................");
            if (!availableTables.isEmpty()) {
                for (String tableId : bookedTables) {
                    logger.info("tableId : {}, Date : {}, locationId : {}, timeSlot : {}, true", tableId, reservationDetails.getDate(), reservationDetails.getLocationId(), reservationDetails.getTimeSlot());
                    tableRepo.updateTable(tableId + "#" + reservationDetails.getDate(),
                            reservationDetails.getLocationId(), reservationDetails.getDate(),
                            reservationDetails.getTimeSlot(), true);
                }

            }

            //update the availability status of newly allotted table to "false" since they have
            //now got booked after editing
            logger.info(".......Updating the status of newly booked tables i.e. setting its availability to false.....");
            logger.info("tableId : {}, Date : {}, locationId : {}, timeSlot : {}, false", availableTables.get(0).getTableNumber(), reservationDetails.getDate(), reservationDetails.getLocationId(), reservationDetails.getTimeSlot());
            tableRepo.updateTable(availableTables.get(0).getTableNumber()
                    , reservationDetails.getLocationId(), reservationDetails.getDate(), timeSlot, false);
        }


        return Map.of("message", "Reservation edited successfully");
    }

    public Map<String,String> deleteReservation(String reservationId) throws Exception
    {
        String reservationIdValidation = ValidationUtil.isValidString(reservationId, "Reservation-ID");
        if(reservationIdValidation != null)
            throw new ValidationException(reservationIdValidation);
        if (!bookingRepo.isReserved(reservationId)) {
            throw new ReservationAlreadyCancelledException("Reservation with ID " + reservationId + " already cancelled.");
        }
        Reservation reservationDetails = bookingRepo.getReservationById(reservationId);
        Location locationDetails = locationRepo.getLocationById(reservationDetails.getLocationId());

        bookingRepo.cancelReservation(reservationId, LocalDateTime.now(ZoneId.of(locationDetails.getZone())));
        for (String tableId : reservationDetails.getTableIds()) {
            tableRepo.updateTable(tableId + "#" + reservationDetails.getDate(),
                    reservationDetails.getLocationId(),
                    reservationDetails.getDate(),
                    reservationDetails.getTimeSlot(),
                    true);
        }

        return Map.of("message", "Cancellation is successful");
    }

    public Map<String, Object> postClientBooking(String queryParams, String customerEmail) throws Exception {
        Reservation reservation = Reservation.fromJson(queryParams);
        if (reservation.getLocationId().isEmpty()) {
            throw new IllegalArgumentException("Missing or invalid Id");
        }
        if (reservation.getGuestsNumber() == 0) {
            throw new IllegalArgumentException("Guests number should not be zero");
        }
        if (reservation.getTableIds().isEmpty()) {
            throw new IllegalArgumentException("Please select at least one table");
        }

        bookingRepo.validateBookingTime(
                reservation.getDate(),
                reservation.getTimeSlot(),
                LocalDateTime.now(
                        ZoneId.of(
                                locationRepo.getLocationById(
                                        reservation.getLocationId()
                                ).getZone()
                        )
                ));
        String locationAddress = locationRepo.getLocationAddress(reservation.getLocationId());

        for (String tableId : reservation.getTableIds()) {
            if (!tableRepo.isTimePresent(reservation.getLocationId(), tableId + "#" + reservation.getDate(), reservation.getTimeSlot()))
                throw new TimeSlotNotFoundException("Invalid date/timeslot provided.");
            if (!tableRepo.isTimeAvailable(reservation.getLocationId(), tableId + "#" + reservation.getDate(), reservation.getTimeSlot()))
                throw new TableAlreadyReservedException("Table already reserved.");
            else
                tableRepo.updateTable(tableId + "#" + reservation.getDate(), reservation.getLocationId(), reservation.getDate(), reservation.getTimeSlot(), false);
        }

        bookingRepo.addReservation(customerEmail, "", "", reservation);

        return Map.of("date", reservation.getDate(),
                "feedbackId", "",
                "guestNumber", reservation.getGuestsNumber(),
                "id", reservation.getId(),
                "locationAddress", locationAddress,
                "preOrder", "",
                "tableNumber", reservation.getTableIds(),
                "timeSlot", reservation.getTimeSlot(),
                "preOrderState", reservation.getPreOrderState());
    }

    public Map<String, Object> postWaiterBooking(String queryParams, String waiterEmail) throws Exception {
        Reservation reservation = Reservation.fromJson(queryParams);
        JSONObject json = new JSONObject(queryParams);
        String clientType = json.optString("clientType", null);

        if (clientType == null || reservation.getDate().isBlank() || reservation.getLocationId().isBlank() || reservation.getTimeFrom().isBlank() || reservation.getTimeTo().isBlank() || reservation.getGuestsNumber() == 0 || reservation.getTableIds().isEmpty()) {
            throw new IllegalArgumentException("Missing required fields");
        } else if (clientType.equals("CUSTOMER") && ((json.optString("customerName", null) == null || json.optString("customerName", null).isBlank()) && (json.optString("customerEmail", null) == null || json.optString("customerEmail", null).isBlank()))) {
            throw new IllegalArgumentException("Missing required fields");
        }

//        if (!customerRepo.isCustomer(json.optString("customerEmail", null))) {
//            throw new UserDoesNotExistsException("Customer not found");
//        }

        Location location = locationRepo.getLocationById(reservation.getLocationId());
        Map<String,Object>waiterDetails=waiterRepo.getWaiterDetails(waiterEmail);
        if (location == null) throw new LocationNotFoundException("Invalid locationId");
        if(!location.getLocationId().equals(waiterDetails.get("location_id").toString()))
            throw new WaiterNotAuthorizedException("Waiter not authorized to book for this location");
        bookingRepo.validateBookingTime(
                reservation.getDate(),
                reservation.getTimeSlot(),
                LocalDateTime.now(
                        ZoneId.of(location.getZone())
                ));

        for (String tableId : reservation.getTableIds()) {
            if (!tableRepo.isTimePresent(reservation.getLocationId(), tableId + "#" + reservation.getDate(), reservation.getTimeSlot()))
                throw new TimeSlotNotFoundException("Invalid date/timeslot provided.");
            if (!tableRepo.isTimeAvailable(reservation.getLocationId(), tableId + "#" + reservation.getDate(), reservation.getTimeSlot()))
                throw new TableAlreadyReservedException("Table already reserved.");
            else
                tableRepo.updateTable(tableId + "#" + reservation.getDate(), reservation.getLocationId(), reservation.getDate(), reservation.getTimeSlot(), false);
        }

        String locationAddress = locationRepo.getLocationAddress(reservation.getLocationId());

        String customerName;
        String customerEmail;
        String userInfo;

        if (clientType.equals("CUSTOMER")) {
            customerName = json.optString("customerName", null);
            customerEmail = json.optString("customerEmail", null);
            if (customerName == null || customerName.isBlank() || customerEmail == null || customerEmail.isBlank()) {
                throw new IllegalArgumentException("Customer name/email is missing");
            }
            userInfo = bookingRepo.addCustomerReservationByWaiter(reservation, customerName, customerEmail, "", waiterEmail);
        } else if (clientType.equals("VISITOR")) {
            userInfo = bookingRepo.addVisitorReservationByWaiter(reservation, "", waiterEmail);
        } else {
            throw new ClientTypeNotFoundException("Client Type not found");
        }

        return Map.of("date", reservation.getDate(),
                "feedbackId", "",
                "guestNumber", reservation.getGuestsNumber(),
                "id", reservation.getId(),
                "locationAddress", locationAddress,
                "preOrder", "",
                "tableNumber", reservation.getTableIds(),
                "timeSlot", reservation.getTimeSlot(),
                "userInfo", userInfo);
    }
}

