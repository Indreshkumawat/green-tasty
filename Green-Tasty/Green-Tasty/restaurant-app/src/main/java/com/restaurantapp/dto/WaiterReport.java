package com.restaurantapp.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WaiterReport {
    String locationId;
    String waiterName;
    String waiterEmail;
    String startDate;
    String endDate;
    double hoursWorked;
    double ordersProcessed;
    List<Double> totalServiceFeedback;
    double minServiceFeedback;
    List<Double> totalCuisineFeedback;
    double minCuisineFeedback;
    double revenue;

    public WaiterReport(String locationId, String waiterName, String waiterEmail, String startDate, String endDate, double hoursWorked, double ordersProcessed, List<Double> totalServiceFeedback, double minServiceFeedback, List<Double> totalCuisineFeedback, double minCuisineFeedback, double revenue) {
        this.locationId = locationId;
        this.waiterName = waiterName;
        this.waiterEmail = waiterEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hoursWorked = hoursWorked;
        this.ordersProcessed = ordersProcessed;
        this.totalServiceFeedback = totalServiceFeedback;
        this.minServiceFeedback = minServiceFeedback;
        this.totalCuisineFeedback = totalCuisineFeedback;
        this.minCuisineFeedback = minCuisineFeedback;
        this.revenue = revenue;
    }

    public static WaiterReport getBlankReport() {
        return new WaiterReport(null, null, null, null, null, 0, 0 , new ArrayList<>() , 0 , new ArrayList<>() , 0, 0);
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getWaiterName() {
        return waiterName;
    }

    public void setWaiterName(String waiterName) {
        this.waiterName = waiterName;
    }

    public String getWaiterEmail() {
        return waiterEmail;
    }

    public void setWaiterEmail(String waiterEmail) {
        this.waiterEmail = waiterEmail;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public double getOrdersProcessed() {
        return ordersProcessed;
    }

    public void setOrdersProcessed(double ordersProcessed) {
        this.ordersProcessed = ordersProcessed;
    }

    public List<Double> getTotalServiceFeedback() {
        return totalServiceFeedback;
    }

    public void setTotalServiceFeedback(List<Double> totalServiceFeedback) {
        this.totalServiceFeedback = totalServiceFeedback;
    }

    public double getMinServiceFeedback() {
        return minServiceFeedback;
    }

    public void setMinServiceFeedback(double minServiceFeedback) {
        this.minServiceFeedback = minServiceFeedback;
    }

    public List<Double> getTotalCuisineFeedback() {
        return totalCuisineFeedback;
    }

    public void setTotalCuisineFeedback(List<Double> totalCuisineFeedback) {
        this.totalCuisineFeedback = totalCuisineFeedback;
    }

    public double getMinCuisineFeedback() {
        return minCuisineFeedback;
    }

    public void setMinCuisineFeedback(double minCuisineFeedback) {
        this.minCuisineFeedback = minCuisineFeedback;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WaiterReport report = (WaiterReport) o;
        return Double.compare(hoursWorked, report.hoursWorked) == 0 && Double.compare(ordersProcessed, report.ordersProcessed) == 0 && Double.compare(minServiceFeedback, report.minServiceFeedback) == 0 && Double.compare(minCuisineFeedback, report.minCuisineFeedback) == 0 && Double.compare(revenue, report.revenue) == 0 && Objects.equals(locationId, report.locationId) && Objects.equals(waiterName, report.waiterName) && Objects.equals(waiterEmail, report.waiterEmail) && Objects.equals(startDate, report.startDate) && Objects.equals(endDate, report.endDate) && Objects.equals(totalServiceFeedback, report.totalServiceFeedback) && Objects.equals(totalCuisineFeedback, report.totalCuisineFeedback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, waiterName, waiterEmail, startDate, endDate, hoursWorked, ordersProcessed, totalServiceFeedback, minServiceFeedback, totalCuisineFeedback, minCuisineFeedback, revenue);
    }

    @Override
    public String toString() {
        return "WaiterReport{" +
                "location='" + locationId + '\'' +
                ", waiterName='" + waiterName + '\'' +
                ", waiterEmail='" + waiterEmail + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", hoursWorked=" + hoursWorked +
                ", ordersProcessed=" + ordersProcessed +
                ", averageServiceFeedback=" + totalServiceFeedback +
                ", minServiceFeedback=" + minServiceFeedback +
                ", averageCuisineFeedback=" + totalCuisineFeedback +
                ", minCuisineFeedback=" + minCuisineFeedback +
                ", revenue=" + revenue +
                '}';
    }
}
