package com.restaurantapp.dto;

import java.util.List;
import java.util.Objects;


public class LocationReport {
    String locationId;
    String startDate;
    String endDate;
    double ordersProcessed;
    List<Double> totalCuisineFeedback;
    double minCuisineFeedback;
    double revenue;

    public LocationReport(String locationId, String startDate, String endDate, double ordersProcessed, List<Double> totalCuisineFeedback, double minCuisineFeedback, double revenue) {
        this.locationId = locationId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ordersProcessed = ordersProcessed;
        this.totalCuisineFeedback = totalCuisineFeedback;
        this.minCuisineFeedback = minCuisineFeedback;
        this.revenue = revenue;
    }

    public static LocationReport getBlankReport(){
        return new LocationReport(null, null, null, 0, List.of(), 0, 0);
    }

    public String getLocationId() {
        return locationId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public double getOrdersProcessed() {
        return ordersProcessed;
    }

    public List<Double> getTotalCuisineFeedback() {
        return totalCuisineFeedback;
    }

    public double getMinCuisineFeedback() {
        return minCuisineFeedback;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setOrdersProcessed(double ordersProcessed) {
        this.ordersProcessed = ordersProcessed;
    }

    public void setTotalCuisineFeedback(List<Double> totalCuisineFeedback) {
        this.totalCuisineFeedback = totalCuisineFeedback;
    }

    public void setMinCuisineFeedback(double minCuisineFeedback) {
        this.minCuisineFeedback = minCuisineFeedback;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LocationReport that = (LocationReport) o;
        return Double.compare(ordersProcessed, that.ordersProcessed) == 0 && Double.compare(minCuisineFeedback, that.minCuisineFeedback) == 0 && Double.compare(revenue, that.revenue) == 0 && Objects.equals(locationId, that.locationId) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(totalCuisineFeedback, that.totalCuisineFeedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, startDate, endDate, ordersProcessed, totalCuisineFeedback, minCuisineFeedback, revenue);
    }

    @Override
    public String toString() {
        return "LocationReport{" +
                "locationId='" + locationId + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", ordersProcessed=" + ordersProcessed +
                ", totalCuisineFeedback=" + totalCuisineFeedback +
                ", minCuisineFeedback=" + minCuisineFeedback +
                ", revenue=" + revenue +
                '}';
    }
}
