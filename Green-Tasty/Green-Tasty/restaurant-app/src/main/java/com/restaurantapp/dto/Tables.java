package com.restaurantapp.dto;

import java.util.Map;
import java.util.Objects;

public class Tables {

    private String tableNumber;
    private String locationId;
    private String capacity;
    private String date;
    private Map<String,Boolean> availableSlots;

    public Tables(String tableNumber, String locationId, String capacity, String date, Map<String, Boolean> availableSlots) {
        this.tableNumber = tableNumber;
        this.locationId = locationId;
        this.capacity = capacity;
        this.date = date;
        this.availableSlots = availableSlots;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getDate() {
        return date;
    }

    public Map<String, Boolean> getAvailableSlots() {
        return availableSlots;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setAvailableSlots(Map<String, Boolean> availableSlots) {
        this.availableSlots = availableSlots;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tables tables = (Tables) o;
        return tableNumber == tables.tableNumber && locationId == tables.locationId && capacity == tables.capacity && Objects.equals(date, tables.date) && Objects.equals(availableSlots, tables.availableSlots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber, locationId, capacity, date, availableSlots);
    }

    @Override
    public String toString() {
        return "Tables{" +
                "tableNumber=" + tableNumber +
                ", locationID=" + locationId +
                ", capacity=" + capacity +
                ", date=" + date +
                ", availableSlots=" + availableSlots +
                '}';
    }
}