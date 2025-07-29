package com.restaurantapp.dto;

import com.restaurantapp.util.DateFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Reservation {

    private final String id;
    private final List<String> tableId;
    private final String locationId;
    private final ReservationStatus status;
    private final String timeSlot;
    private final int guestsNumber;
    private final String feedbackId;
    private final String date;
    private final String timeFrom;
    private final String timeTo;
    private String locationAddress;
    private Map<String,String> preOrder;
    private String preOrderState;
    private String customerName;
    private String waiterName;
    private String visitorId;

    public Reservation(List<String> tableId, String locationId, ReservationStatus status, String timeSlot, int guestsNumber, String feedbackId, String date, String timeFrom, String timeTo, Map<String,String> preOrder , String preOrderState) {
        this.id = UUID.randomUUID().toString();
        this.tableId = tableId;
        this.locationId = locationId;
        this.status = status;
        this.timeSlot = timeSlot;
        this.guestsNumber = guestsNumber;
        this.feedbackId = feedbackId;
        this.date = date;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.preOrderState = preOrderState;
        this.preOrder = preOrder;
    }

    public Reservation(String id, List<String> tableId, String locationId, ReservationStatus status, String timeSlot, int guestsNumber, String feedbackId, String date, String timeFrom, String timeTo, Map<String,String> preOrder , String preOrderState) {
        this.id = id;
        this.tableId = tableId;
        this.locationId = locationId;
        this.status = status;
        this.timeSlot = timeSlot;
        this.guestsNumber = guestsNumber;
        this.feedbackId = feedbackId;
        this.date = date;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.preOrderState = preOrderState;
        this.preOrder = preOrder;
    }


    public static Reservation fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String locationId = json.optString("locationId", null);
        JSONArray tableIdArray = json.optJSONArray("tableNumber", null);
        String date = DateFormatter.convertToStandardFormat(json.optString("date", null));
        int guestsNumber = json.optInt("guestsNumber", 0);
        String timeFrom = json.optString("timeFrom", null);
        String timeTo = json.optString("timeTo", null);
        String feedbackId = json.optString("feedbackId", null);
        ReservationStatus status = json.optEnum(ReservationStatus.class, ReservationStatus.NONE.toString());
        String timeSlot = timeFrom + "-" + timeTo;

        List<String> tableId = new ArrayList<>();
        if (tableIdArray != null) {
            for (int i = 0; i < tableIdArray.length(); i++) {
                tableId.add(tableIdArray.optString(i));
            }
        }

        JSONObject preOrderJson = json.optJSONObject("preOrder");
        Map<String, String> preOrder = new HashMap<>();
        if (preOrderJson != null) {  // Add this null check to avoid NullPointerException
            for (String key : preOrderJson.keySet()) {
                preOrder.put(key, preOrderJson.optString(key, ""));
            }
        }

        String preOrderState = json.optString("preOrderState", "");
        return new Reservation(tableId, locationId, status, timeSlot, guestsNumber, feedbackId, date, timeFrom, timeTo , preOrder , preOrderState);
    }

    public String getId() {
        return id;
    }

    public String getLocationId() {
        return locationId;
    }

    public List<String> getTableIds() {
        return tableId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public int getGuestsNumber() {
        return guestsNumber;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public String getDate() {
        return date;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public Map<String,String> getPreOrder() {
        return preOrder;
    }

    public String getPreOrderState(){ return preOrderState; }

    public String getCustomerName() { return customerName; }

    public String getWaiterName() { return waiterName; }

    public String getVisitorId() { return visitorId; }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public void setVisitorId(String visitorId) {
        this.visitorId = visitorId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return guestsNumber == that.guestsNumber && Objects.equals(id, that.id) && Objects.equals(tableId, that.tableId) && Objects.equals(locationId, that.locationId) && status == that.status && Objects.equals(timeSlot, that.timeSlot) && Objects.equals(feedbackId, that.feedbackId) && Objects.equals(date, that.date) && Objects.equals(timeFrom, that.timeFrom) && Objects.equals(timeTo, that.timeTo) && Objects.equals(locationAddress, that.locationAddress) && Objects.equals(preOrder, that.preOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tableId, locationId, status, timeSlot, guestsNumber, feedbackId, date, timeFrom, timeTo, locationAddress, preOrder);
    }
}
