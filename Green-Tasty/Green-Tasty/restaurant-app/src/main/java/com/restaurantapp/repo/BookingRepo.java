package com.restaurantapp.repo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.NameMap;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import com.restaurantapp.exception.ReservationAlreadyCancelledException;
import com.restaurantapp.exception.ReservationCancellationOrModificationException;
import com.restaurantapp.exception.ReservationNotFoundException;
import com.restaurantapp.dto.PreOrderState;
import com.restaurantapp.dto.Reservation;
import com.restaurantapp.dto.ReservationStatus;
import com.restaurantapp.dto.Tables;
import com.restaurantapp.exception.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class BookingRepo {
    AmazonDynamoDB amazonDynamoDBClient;
    DynamoDB dynamoDBClient;
    Table reservationTable;
    Table tablesTable;
    Table waiterTable;
    Table dishLocationTable;

    CustomerRepo customerRepo;
    LocationRepo locationRepo;
    WaiterRepo waiterRepo;


    @Autowired
    public BookingRepo(
            AmazonDynamoDB amazonDynamoDBClient,
            LocationRepo locationRepo,
            WaiterRepo waiterRepo,
            CustomerRepo customerRepo,
            String reservationTableName,
            String tablesTableName,
            String waiterTableName,
            String dishLocationTableName
    ) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDBClient = new DynamoDB(amazonDynamoDBClient);
        this.locationRepo = locationRepo;
        this.waiterRepo = waiterRepo;
        this.customerRepo = customerRepo;

        this.reservationTable = dynamoDBClient.getTable(reservationTableName);
        this.tablesTable = dynamoDBClient.getTable(tablesTableName);
        this.waiterTable = dynamoDBClient.getTable(waiterTableName);
        this.dishLocationTable = dynamoDBClient.getTable(dishLocationTableName);
    }

    public boolean isReserved(String id) throws ReservationNotFoundException {
        try {
            Item reservationItem = reservationTable.getItem("reservation_id", id);
            String reservationStatus = reservationItem.getString("status");
            return (reservationStatus.equalsIgnoreCase("reserved"));
        } catch (Exception e) {
            throw new ReservationNotFoundException("No reservation with ID " + id + " found.");
        }
    }

    public void addReservation(String customerEmail, String waiterEmail, String visitor_id, Reservation reservation) throws Exception {
        try {
            waiterEmail = waiterRepo.getLeastBusyWaiter(reservation.getLocationId(), reservation.getDate());
            reservationTable.putItem(new Item()
                    .withPrimaryKey("reservation_id", reservation.getId())
                    .withList("table_id", reservation.getTableIds())
                    .withString("location_id", reservation.getLocationId())
                    .withString("status", ReservationStatus.RESERVED.toString())
                    .withString("date", reservation.getDate())
                    .withString("time_slot", reservation.getTimeSlot())
                    .withMap("pre_order", reservation.getPreOrder())
                    .withString("pre_order_state", reservation.getPreOrderState())
                    .withInt("guests_number", reservation.getGuestsNumber())
                    .withString("feedback_id", "")
                    .withString("customer_email", customerEmail)
                    .withString("visitor_id", visitor_id)
                    .withString("waiter_email", waiterEmail)
            );
            if (!customerEmail.isBlank())
                waiterRepo.updateWaiterBusyCount(waiterEmail, reservation.getDate(), "customer_count", 1);
            else waiterRepo.updateWaiterBusyCount(waiterEmail, reservation.getDate(), "visitor_count", 1);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            System.out.println(e.getMessage());
            throw new Exception("Failed to add Booking.");
        }
    }

    public String addCustomerReservationByWaiter(Reservation reservation, String customerName, String customerEmail, String feedbackId, String waiterEmail) throws Exception {
        try {
            if (!customerRepo.isCustomer(customerEmail)) throw new Exception("Customer not found");
            String userInfo;

            reservationTable.putItem(new Item()
                    .withPrimaryKey("reservation_id", reservation.getId())
                    .withList("table_id", reservation.getTableIds())
                    .withString("location_id", reservation.getLocationId())
                    .withString("status", ReservationStatus.RESERVED.toString())
                    .withString("date", reservation.getDate())
                    .withString("time_slot", reservation.getTimeSlot())
                    .withMap("pre_order", reservation.getPreOrder())
                    .withString("pre_order_state", reservation.getPreOrderState())
                    .withInt("guests_number", reservation.getGuestsNumber())
                    .withString("feedback_id", feedbackId)
                    .withString("customer_email", customerEmail)
                    .withString("waiter_email", waiterEmail)
            );
            waiterRepo.updateWaiterBusyCount(waiterEmail, reservation.getDate(), "customer_count", 1);
            userInfo = "Customer " + customerName;
            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String addVisitorReservationByWaiter(Reservation reservation, String feedbackId, String waiterEmail) throws Exception {
        try {
            waiterRepo.updateWaiterBusyCount(waiterEmail, reservation.getDate(), "visitor_count", 1);
            String userInfo;
            String visitorId = waiterRepo.getVisitorCount(waiterEmail);

            reservationTable.putItem(new Item()
                    .withPrimaryKey("reservation_id", reservation.getId())
                    .withList("table_id", reservation.getTableIds())
                    .withString("location_id", reservation.getLocationId())
                    .withString("status", ReservationStatus.RESERVED.toString())
                    .withString("date", reservation.getDate())
                    .withString("time_slot", reservation.getTimeSlot())
                    .withMap("pre_order", reservation.getPreOrder())
                    .withString("pre_order_state", reservation.getPreOrderState())
                    .withInt("guests_number", reservation.getGuestsNumber())
                    .withString("feedback_id", "feedbackId")
                    .withString("visitor_id", visitorId)
                    .withString("waiter_email", waiterEmail)
            );
            userInfo = "Visitor " + visitorId;
            return userInfo;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    public void validateBookingTime(String date, String timeSlot, LocalDateTime currentDateTime) throws ReservationBookingTimeException {
        String[] timeParts = timeSlot.split("-");
        String timeFrom = timeParts[0];

        LocalDate reservationDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalTime reservationStartTime = LocalTime.parse(timeFrom, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationStartTime);

        long timeDifference = ChronoUnit.MINUTES.between(currentDateTime, reservationDateTime);
        if (timeDifference < 0) {
            throw new ReservationBookingTimeException("Oops! You can't book a time in the past. Please pick a future time to proceed.");
        }
    }

    public void validateCancellationOrModificationTime(String date, String timeSlot, LocalDateTime currentDateTime) throws ReservationCancellationOrModificationException {
        String[] timeParts = timeSlot.split("-");
        String timeFrom = timeParts[0];

        LocalDate reservationDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalTime reservationStartTime = LocalTime.parse(timeFrom, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationStartTime);

        long timeDifference = ChronoUnit.MINUTES.between(currentDateTime, reservationDateTime);
        if (timeDifference < 30) {
            throw new ReservationCancellationOrModificationException("Cancellation or Modification is only allowed up to 30 minutes or more before the reservation start time.");
        }
    }

    public void updateReservationStatus(String reservationId, String newStatus) throws Exception {
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("SET #status = :newStatus")
                    .withNameMap(new NameMap().with("#status", "status"))
                    .withValueMap(new ValueMap().with(":newStatus", newStatus));

            reservationTable.updateItem(updateItemSpec);
            System.out.println("Reservation ID: " + reservationId + " status updated to: " + newStatus);
        } catch (Exception e) {
            throw new Exception("Failed to update reservation status in Reservation Table: " + e.getMessage());
        }
    }


    public void updateReservationWithPreOrderDetails(String reservationId,
                                                     String newPreOrderState,
                                                     Map<String, String> preOrder,
                                                     LocalDateTime currentDateTime) throws Exception {

        // Get reservation and current state
        Reservation reservation = getReservationById(reservationId);
        Item item = reservationTable.getItem("reservation_id", reservationId);
        String currentPreOrderState = item.getString("pre_order_state");

        // Handle empty pre-order case
        if (preOrder != null && preOrder.isEmpty()) {
            newPreOrderState = "";
            validateCancellationOrModificationTime(reservation.getDate(), reservation.getTimeSlot(), currentDateTime);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("SET pre_order = :p, pre_order_state = :s")
                    .withValueMap(new ValueMap()
                            .withMap(":p", preOrder)
                            .withString(":s", newPreOrderState)
                    );
            reservationTable.updateItem(updateItemSpec);
            return;
        }

        // Validate state transition
        validateStateTransition(currentPreOrderState, newPreOrderState);

        // Perform time validation if required
        if (requiresTimeValidation(currentPreOrderState, newPreOrderState)) {
            validateCancellationOrModificationTime(reservation.getDate(), reservation.getTimeSlot(), currentDateTime);
        }

        // Update table
        UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("reservation_id", reservationId)
                .withUpdateExpression("SET pre_order = :p, pre_order_state = :s")
                .withValueMap(new ValueMap()
                        .withMap(":p", preOrder)
                        .withString(":s", newPreOrderState)
                );
        reservationTable.updateItem(updateItemSpec);
    }

    private void validateStateTransition(String currentState, String newState) throws PreOrderStateChangeException {
        // If current state is EDIT_IN_PROGRESS
        if (currentState.equalsIgnoreCase(PreOrderState.EDIT_IN_PROGRESS.name()) && newState.equalsIgnoreCase(PreOrderState.UNSUBMITTED.name())) {
            throw new PreOrderStateChangeException("From EDIT_IN_PROGRESS state, you can only change to SUBMITTED or stay in EDIT_IN_PROGRESS");
        }
        // If current state is UNSUBMITTED
        else if (currentState.equalsIgnoreCase(PreOrderState.UNSUBMITTED.name()) && newState.equalsIgnoreCase(PreOrderState.EDIT_IN_PROGRESS.name())) {
            throw new PreOrderStateChangeException("From UNSUBMITTED state, you can only change to SUBMITTED or stay in UNSUBMITTED");
        }

        // If current state is SUBMITTED
        else if (currentState.equalsIgnoreCase(PreOrderState.SUBMITTED.name()) && !(newState.equalsIgnoreCase(PreOrderState.EDIT_IN_PROGRESS.name())
                || newState.equalsIgnoreCase(""))) {
            throw new PreOrderStateChangeException("From SUBMITTED state, you can only change to EDIT_IN_PROGRESS");
        }

    }


    private boolean requiresTimeValidation(String currentState, String newState) {
        // Only validate time when transitioning from UNSUBMITTED to SUBMITTED
        // or from SUBMITTED to EDIT_IN_PROGRESS
        return (currentState.equalsIgnoreCase(PreOrderState.UNSUBMITTED.name()) &&
                newState.equalsIgnoreCase(PreOrderState.SUBMITTED.name())) ||
                (currentState.equalsIgnoreCase(PreOrderState.SUBMITTED.name()) &&
                        newState.equalsIgnoreCase(PreOrderState.EDIT_IN_PROGRESS.name()));
    }

    public void cancelReservation(String reservationId, LocalDateTime currentDateTime) throws Exception {
        try {
            if (!isReserved(reservationId)) {
                throw new ReservationAlreadyCancelledException("Reservation with ID " + reservationId + " already finished or cancelled.");
            }

            Item reservationItem = reservationTable.getItem("reservation_id", reservationId);
            String timeSlot = reservationItem.getString("time_slot");
            String date = reservationItem.getString("date");
            String waiterEmail = reservationItem.get("waiter_email").toString();
            String customerEmail = "";
            if (reservationItem.get("customer_email") != null)
                customerEmail = reservationItem.get("customer_email").toString();

            validateCancellationOrModificationTime(date, timeSlot, currentDateTime);

            updateReservationStatus(reservationItem.getString("reservation_id"), ReservationStatus.CANCELLED.toString());

            if (customerEmail.isBlank()) waiterRepo.updateWaiterBusyCount(waiterEmail, date, "visitor_count", -1);
            else waiterRepo.updateWaiterBusyCount(waiterEmail, date, "customer_count", -1);
        } catch (ReservationAlreadyCancelledException | ReservationNotFoundException |
                 ReservationCancellationOrModificationException e) {
            throw e;
        } catch (Exception e) {
            throw new Exception("Cancellation failed : " + e.getMessage());
        }
    }

    public Reservation getReservationById(String reservationId) throws ReservationNotFoundException {
        Item reservationItem = reservationTable.getItem("reservation_id", reservationId);
        if (reservationItem == null) {
            // Step 2: Throw ReservationNotFoundException if no item is found
            throw new ReservationNotFoundException("Reservation with ID " + reservationId + " not found.");
        }
        return new Reservation(reservationItem.getList("table_id"),
                reservationItem.getString("location_id"),
                ReservationStatus.valueOf(reservationItem.getString("status").toUpperCase()),
                reservationItem.getString("time_slot"),
                Integer.parseInt(reservationItem.getString("guests_number")),
                reservationItem.getString("feedback_id"),
                reservationItem.getString("date"),
                reservationItem.getString("time_slot").split("-")[0],
                reservationItem.getString("time_slot").split("-")[1],
                reservationItem.getMap("pre_order"),
                reservationItem.getString("pre_order_state")
        );
    }

    public Map<String, Object> getReservation(String reservationId) throws ReservationNotFoundException {
        Item reservationItem = reservationTable.getItem("reservation_id", reservationId);
        if (reservationItem == null) {
            // Step 2: Throw ReservationNotFoundException if no item is found
            throw new ReservationNotFoundException("Reservation with ID " + reservationId + " not found.");
        }
        return reservationItem.asMap();
    }

    public List<Map<String, Object>> getAllReservation(String userEmail, String dateReq, String time, String tableNumber) throws Exception {
        try {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            StringBuilder filter = new StringBuilder();
            Map<String, String> expressionAttributeNames = null;
            String waiterName = "";
            if (waiterRepo.isWaiter(userEmail)) {
                expressionAttributeValues.put(":waiter_email", new AttributeValue().withS(userEmail));
                filter.append("waiter_email = :waiter_email");
                if (dateReq != null) {
                    expressionAttributeValues.put(":dateReq", new AttributeValue().withS(dateReq));
                    filter.append(" AND #dateAlias = :dateReq");
                    expressionAttributeNames = Map.of("#dateAlias", "date");
                }
                if (tableNumber != null) {
                    expressionAttributeValues.put(":table_num", new AttributeValue().withS(tableNumber));
                    filter.append(" AND contains(table_id, :table_num)");
                }
                if (time != null) {
                    expressionAttributeValues.put(":time", new AttributeValue().withS(time));
                    filter.append(" AND begins_with(time_slot, :time)");
                }
                Map<String, Object> waiterDetails = waiterRepo.getWaiterDetails(userEmail);
                waiterName = waiterDetails.get("first_name") + " " + waiterDetails.get("last_name");
            } else {
                expressionAttributeValues = Map.of(":customer_email", new AttributeValue().withS(userEmail));
                filter.append("customer_email = :customer_email");
            }
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(reservationTable.getTableName())
                    .withFilterExpression(filter.toString())
                    .withExpressionAttributeNames(expressionAttributeNames)
                    .withExpressionAttributeValues(expressionAttributeValues);

            ScanResult scanResult = amazonDynamoDBClient.scan(scanRequest);
            List<Map<String, AttributeValue>> items = scanResult.getItems();
            List<Map<String, Object>> reservations = new ArrayList<>();


            for (Map<String, AttributeValue> item : items) {
                Map<String, String> preOrder = new HashMap<>();
                String id = item.get("reservation_id").getS();
                String timeSlot = item.get("time_slot").getS();
                String date = item.get("date").getS();
                ReservationStatus reservationStatus = ReservationStatus.valueOf(item.get("status").getS());
                String customerName = "", visitor = "";
                if (!waiterName.isBlank()) {
                    if (item.containsKey("customer_email")) {
                        String customerEmail = item.get("customer_email").getS();
                        Map<String, Object> customerDetails = customerRepo.getCustomerDetails(customerEmail);
                        customerName = customerDetails.get("first_name").toString() + " " + customerDetails.get("last_name").toString();
                    } else {
                        visitor = item.get("visitor_id").getS();
                    }
                }

                int guestNumber = Integer.parseInt(item.get("guests_number").getN());
                String feedbackId = item.get("feedback_id").getS();
                String locationAddress = locationRepo.getLocationAddress(item.get("location_id").getS());
                String preOrderState = item.get("pre_order_state").getS();
                Map<String, AttributeValue> preOrderMap = item.get("pre_order").getM();
                for (Map.Entry<String, AttributeValue> entry : preOrderMap.entrySet()) {
                    preOrder.put(entry.getKey(), entry.getValue().getS());
                }
                String waiterEmail = item.get("waiter_email").getS();
                List<String> tables = item.get("table_id").getL()
                        .stream()
                        .map(AttributeValue::getS)
                        .toList();

                Map<String, Object> reservationDetails = new HashMap<>();
                reservationDetails.put("id", id);
                reservationDetails.put("status", reservationStatus);
                reservationDetails.put("timeSlot", timeSlot);
                reservationDetails.put("guestsNumber", guestNumber);
                reservationDetails.put("feedbackId", feedbackId);
                reservationDetails.put("date", date);
                reservationDetails.put("preOrder", preOrder);
                reservationDetails.put("locationAddress", locationAddress);
                reservationDetails.put("waiterEmail", waiterEmail);
                reservationDetails.put("preOrderState", preOrderState);
                if (customerRepo.isCustomer(userEmail)) {
                    reservationDetails.put("tableNumber", tables);
                    reservations.add(reservationDetails);
                } else if (!customerName.isBlank()) {
                    reservationDetails.put("customerName", customerName);
                    reservationDetails.put("waiterName", waiterName);
                    if (tableNumber != null) reservationDetails.put("tableNumber", List.of(tableNumber));
                    else reservationDetails.put("tableNumber", tables);
                    reservations.add(reservationDetails);
                } else {
                    reservationDetails.put("waiterName", waiterName);
                    reservationDetails.put("visitorId", visitor);
                    if (tableNumber != null) reservationDetails.put("tableNumber", List.of(tableNumber));
                    else reservationDetails.put("tableNumber", tables);
                    reservations.add(reservationDetails);
                }
            }
            return reservations;
        } catch (Exception e) {
            throw new Exception("Error fetching reservations from DynamoDB: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllReservationWithReservedStatus(String customerEmail) throws Exception {
        // This returns a D.S which has only the fields required for CartResponse.
        //            ScanRequest scanRequest = new ScanRequest()
//                    .withTableName(reservationTable.getTableName())
//                    .withFilterExpression("customer_email = :email AND #s = :status")
//                    .withExpressionAttributeNames(Map.of(
//                            "#s", "status"
//                    ))
//                    .withExpressionAttributeValues(Map.of(
//                            ":email", new AttributeValue().withS(customerEmail),
//                            ":status", new AttributeValue().withS("RESERVED")
//                    ));
//
//            ScanResult response = amazonDynamoDBClient.scan(scanRequest);
        try {
            QueryRequest queryRequest = new QueryRequest()
                    .withTableName(reservationTable.getTableName())
                    .withIndexName("customer_email-status-index") // GSI name
                    .withKeyConditionExpression("customer_email = :email AND #s = :status")
                    .withExpressionAttributeNames(Map.of(
                            "#s", "status"
                    ))
                    .withExpressionAttributeValues(Map.of(
                            ":email", new AttributeValue().withS(customerEmail),
                            ":status", new AttributeValue().withS("RESERVED")
                    ));

            QueryResult response = amazonDynamoDBClient.query(queryRequest);

            List<Map<String, AttributeValue>> items = response.getItems();

            List<Map<String, Object>> reservations = new ArrayList<>();

            for (Map<String, AttributeValue> item : items) {
                Map<String, String> preOrder = new HashMap<>();
                String id = item.get("reservation_id").getS();
                String timeSlot = item.get("time_slot").getS();
                String date = item.get("date").getS();
                String locationAddress = locationRepo.getLocationAddress(item.get("location_id").getS());
                String preOrderState = item.get("pre_order_state").getS();
                Map<String, AttributeValue> preOrderMap = item.get("pre_order").getM();
                for (Map.Entry<String, AttributeValue> entry : preOrderMap.entrySet()) {
                    preOrder.put(entry.getKey(), entry.getValue().getS());
                }

                Map<String, Object> reservationDetails = new HashMap<>();
                reservationDetails.put("reservationId", id);
                reservationDetails.put("timeSlot", timeSlot);
                reservationDetails.put("date", date);
                reservationDetails.put("preOrder", preOrder);
                reservationDetails.put("locationAddress", locationAddress);
                reservationDetails.put("state", preOrderState);
                reservations.add(reservationDetails);

            }

            return reservations;

        } catch (Exception e) {
            LoggerFactory.getLogger(BookingRepo.class).info(e.getMessage());
            throw new Exception("Error fetching reservations from DynamoDB");
        }

    }

    public void addDishToCartForReservation(String reservationId, String dishId, LocalDateTime currentDateTime) throws ReservationNotFoundException,IllegalStateException, ReservationCancellationOrModificationException, RuntimeException {
        try {
            // Retrieve the reservation by ID
            Item reservationItem = reservationTable.getItem("reservation_id", reservationId);
            if (reservationItem == null) {
                throw new ReservationNotFoundException("Reservation with ID " + reservationId + " not found.");
            }

            // Check the reservation status
            String status = reservationItem.getString("status").toUpperCase();
            if (ReservationStatus.CANCELLED.name().equals(status)
                    || ReservationStatus.FINISHED.name().equals(status)
                    || ReservationStatus.PENDING_REVIEW.name().equals(status)) {
                throw new IllegalStateException("Cannot add dish to a reservation that is " + status.toLowerCase() + ".");
            }

            String locationId = reservationItem.getString("location_id"); // Extract locationId from reservation

            Item dishLocationItem = dishLocationTable.getItem("location_id", locationId, "dish_id", dishId);
            if (dishLocationItem == null) {
                throw new IllegalStateException("Dish with ID " + dishId + " is not available for location ID " + locationId + ".");
            }

            String availability = dishLocationItem.getString("availability"); // Retrieve availability
            if ("on stop".equalsIgnoreCase(availability)) {
                throw new IllegalStateException("Dish with ID " + dishId + " is currently 'on stop' and cannot be added to the preorder.");
            }

            // Step 3: Validate time to allow dish addition using cancellation time logic
            String timeSlot = reservationItem.getString("time_slot"); // Extract time slot from reservation
            String date = reservationItem.getString("date"); // Extract date from reservation
            validateCancellationOrModificationTime(date, timeSlot, currentDateTime);

            // Retrieve the current pre-order map
            Map<String, String> preOrder = reservationItem.getMap("pre_order");
            if (preOrder == null) { // Initialize the map if it doesn't exist
                preOrder = new HashMap<>();
            }

            // Increment or add dish count
            preOrder.put(dishId, String.valueOf(Integer.parseInt(preOrder.getOrDefault(dishId, "0")) + 1));

            String preOrderState = reservationItem.getString("pre_order_state");
            preOrderState = "UNSUBMITTED"; // Set state to UNSUBMITTED

            // Build the UpdateItemSpec to update both pre_order and pre_order_state fields in DynamoDB
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("SET #preOrder = :preOrder, #preOrderState = :preOrderState") // Update both fields
                    .withNameMap(new NameMap()
                            .with("#preOrder", "pre_order")
                            .with("#preOrderState", "pre_order_state")) // Map the attribute names
                    .withValueMap(new ValueMap()
                            .withMap(":preOrder", preOrder) // Provide preOrder map for the update
                            .withString(":preOrderState", preOrderState)); // Provide preOrderState value for the update

            reservationTable.updateItem(updateItemSpec);

        } catch (IllegalStateException e) {
            // For invalid reservation state
            throw e;
        } catch (ReservationNotFoundException e) {
            // For reservation not found
            throw e;
        } catch (ReservationCancellationOrModificationException e) {
            // For time-based restriction checks
            throw e;
        } catch (RuntimeException e) {
            // Generic exception handling
            throw new RuntimeException("Failed to add dish to reservation: " + e.getMessage(), e);
        }
    }

    public void editReservation(List<Tables> tableList, String reservationId, String timeSlot, int guestsNumber, LocalDateTime currentTime) throws TableNotAvailableException, ReservationNotFoundException, ReservationBookingTimeException, ReservationCancellationOrModificationException, ReservationAlreadyCancelledException {
        Reservation reservationDetails = getReservationById(reservationId);
        validateCancellationOrModificationTime(reservationDetails.getDate(), reservationDetails.getTimeSlot(), currentTime);
        validateBookingTime(reservationDetails.getDate(), reservationDetails.getTimeSlot(), currentTime);

        if (tableList == null) {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("set guests_number = :g")
                    .withValueMap(new ValueMap()
                            .withNumber(":g", guestsNumber))
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            reservationTable.updateItem(updateItemSpec);
        } else {
            if (!tableList.isEmpty()) {
                Tables firstTable = tableList.get(0);
                String firstTimeSlot = firstTable.getAvailableSlots().keySet().iterator().next();
                if (!firstTimeSlot.equals(timeSlot))
                    throw new TableNotAvailableException("No available tables match your updated reservation criteria ", "PATCH");
            } else
                throw new TableNotAvailableException("No available tables match your updated reservation criteria ", "PATCH");


            //the first entry in tableList always satisfies the guest count so just choosing that
            List<String> tableId = new ArrayList<>();
            tableId.add(tableList.get(0).getTableNumber().split("#")[0]);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("set table_id = :t, time_slot = :ts, guests_number = :g")
                    .withValueMap(new ValueMap()
                            .withList(":t", tableId)
                            .withString(":ts", timeSlot)
                            .withNumber(":g", guestsNumber))
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            reservationTable.updateItem(updateItemSpec);
        }
    }

    public void updateReservationFeedback(String reservationId, String feedbackId) throws RuntimeException {
        try {
            Item reservationItem = reservationTable.getItem("reservation_id", reservationId);

            UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                    .withPrimaryKey("reservation_id", reservationId)
                    .withUpdateExpression("set feedback_id= :f")
                    .withValueMap(new ValueMap()
                            .withString(":f", feedbackId))
                    .withReturnValues(ReturnValue.UPDATED_NEW);
            reservationTable.updateItem(updateItemSpec);

        } catch (RuntimeException e) {
            throw new RuntimeException("Error updating the feedbackId in reservation table: " + e.getMessage());

        }
    }

    public boolean feedbackExistsByReservationId(String reservationId) {
        Item reservationItem = reservationTable.getItem("reservation_id", reservationId);
        String existingFeedbackId = reservationItem.getString("feedback_id");
        if (!existingFeedbackId.isEmpty()) {
            return true;
        }
        return false;
    }


    public String getWaiterEmailByReservationId(String reservationId) throws ReservationNotFoundException, RuntimeException {
        try {
            // Retrieve the reservation from the Reservation Table using reservationId
            Item reservationItem = reservationTable.getItem("reservation_id", reservationId);

            if (reservationItem == null) {
                throw new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
            }

            // Get the waiterId from the reservation item
            return reservationItem.getString("waiter_email");
        } catch (RuntimeException e) {
            throw new RuntimeException("Error retrieving waiter ID: " + e.getMessage(), e);
        }
    }
}
