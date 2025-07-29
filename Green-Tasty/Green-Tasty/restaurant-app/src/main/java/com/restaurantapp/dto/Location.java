package com.restaurantapp.dto;

import java.util.Map;
import java.util.Objects;

public final class Location {
    private String locationId;
    private String address;
    private String description;
    private String totalCapacity;
    private String averageOccupancy;
    private String imageUrl;
    private String rating;
    private String zone;

    public Location() {}


    public Location(String locationId, String address, String description, String totalCapacity, String averageOccupancy, String imageUrl, String rating, String zone) {
        this.locationId = locationId;
        this.address = address;
        this.description = description;
        this.totalCapacity = totalCapacity;
        this.averageOccupancy = averageOccupancy;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.zone = zone;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getTotalCapacity() {
        return totalCapacity;
    }

    public String getAverageOccupancy() {
        return averageOccupancy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRating() {
        return rating;
    }

    public String getZone() {
        return zone;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTotalCapacity(String totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public void setAverageOccupancy(String averageOccupancy) {
        this.averageOccupancy = averageOccupancy;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public static Location fromMap(Map<String, Object> attributes) {
        return new Location(
                (String) attributes.get("location_id"),
                (String) attributes.get("address"),
                (String) attributes.get("description"),
                (String) attributes.get("totalCapacity"),
                (String) attributes.get("averageOccupancy"),
                (String) attributes.get("imageUrl"),
                (String) attributes.get("rating"),
                (String) attributes.get("zone")
        );
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(locationId, location.locationId)
                && Objects.equals(address, location.address)
                && Objects.equals(description, location.description)
                && Objects.equals(totalCapacity, location.totalCapacity)
                && Objects.equals(averageOccupancy, location.averageOccupancy)
                && Objects.equals(imageUrl, location.imageUrl)
                && Objects.equals(rating, location.rating)
                && Objects.equals(zone, location.zone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId, address, description, totalCapacity, averageOccupancy, imageUrl, rating, zone);
    }

    @Override
    public String toString() {
        return "Location{" +
                "locationId='" + locationId + '\'' +
                ", address='" + address + '\'' +
                ", description='" + description + '\'' +
                ", totalCapacity='" + totalCapacity + '\'' +
                ", averageOccupancy='" + averageOccupancy + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", rating='" + rating + '\'' +
                ", zone='" + zone + '\'' +
                '}';
    }


}