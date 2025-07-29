package com.restaurantapp.dto;

import org.json.JSONObject;

import java.util.Objects;

public class Feedback {

    private final String id;

    private final String reservationId;

    private final String cuisineComment;

    private final float cuisineRating;

    private final String serviceComment;

    private final float serviceRating;

    public Feedback(String id, String reservationId, String cuisineComment, float cuisineRating, String serviceComment, float serviceRating) {

        this.id = id;

        this.reservationId = reservationId;

        this.cuisineComment = cuisineComment;

        this.cuisineRating = cuisineRating;

        this.serviceComment = serviceComment;

        this.serviceRating = serviceRating;

    }

    public String getId() {

        return id;

    }

    public String getReservationId() {

        return reservationId;

    }

    public String getCuisineComment() {
        return cuisineComment;
    }


    public float getCuisineRating() {

        return cuisineRating;

    }

    public String getServiceComment() {

        return serviceComment;

    }

    public float getServiceRating() {

        return serviceRating;

    }

    public static Feedback fromJson(String jsonString) {
        JSONObject json = new JSONObject(jsonString);
        String feedbackId = json.optString("feedbackId", null);
        String reservationId = json.optString("reservationId", null);
        String cuisineComment = json.optString("cuisineComment", "");
        float cuisineRating = json.optFloat("cuisineRating");
        String serviceComment = json.optString("serviceComment", "");
        float serviceRating = json.optFloat("serviceRating");

        return new FeedbackBuilder().setFeedbackId(feedbackId).setReservationId(reservationId).setCuisineComment(cuisineComment).setCuisineRating(cuisineRating).setServiceComment(serviceComment).setServiceRating(serviceRating).createFeedback();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Feedback feedback = (Feedback) o;
        return Float.compare(cuisineRating, feedback.cuisineRating) == 0 && Float.compare(serviceRating, feedback.serviceRating) == 0 && Objects.equals(id, feedback.id) && Objects.equals(reservationId, feedback.reservationId) && Objects.equals(cuisineComment, feedback.cuisineComment) && Objects.equals(serviceComment, feedback.serviceComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationId, cuisineComment, cuisineRating, serviceComment, serviceRating);
    }

    public static class FeedbackBuilder {
        private String feedbackId;
        private String reservationId;
        private String cuisineComment = "";
        private float cuisineRating;
        private String serviceComment = "";
        private float serviceRating;

        public FeedbackBuilder setFeedbackId(String feedbackId) {
            this.feedbackId = feedbackId;
            return this;
        }

        public FeedbackBuilder setReservationId(String reservationId) {
            this.reservationId = reservationId;
            return this;
        }

        public FeedbackBuilder setCuisineComment(String cuisineComment) {
            this.cuisineComment = cuisineComment;
            return this;
        }

        public FeedbackBuilder setCuisineRating(float cuisineRating) {
            this.cuisineRating = cuisineRating;
            return this;
        }

        public FeedbackBuilder setServiceComment(String serviceComment) {
            this.serviceComment = serviceComment;
            return this;
        }

        public FeedbackBuilder setServiceRating(float serviceRating) {
            this.serviceRating = serviceRating;
            return this;
        }

        public Feedback createFeedback() {
            return new Feedback(feedbackId, reservationId, cuisineComment, cuisineRating, serviceComment, serviceRating);
        }
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id='" + id + '\'' +
                ", reservationId='" + reservationId + '\'' +
                ", cuisineComment='" + cuisineComment + '\'' +
                ", cuisineRating=" + cuisineRating +
                ", serviceComment='" + serviceComment + '\'' +
                ", serviceRating=" + serviceRating +
                '}';
    }
}


