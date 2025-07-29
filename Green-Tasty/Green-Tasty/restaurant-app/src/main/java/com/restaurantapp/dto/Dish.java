package com.restaurantapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dish {
    private final String name;
    private final String id;
    private final String price;
    private final String weight;
    private final String imageUrl;
    private final String description;
    private final String dishType;
    private final String state;
    private final String calories;
    private final String carbohydrates;
    private final String fats;
    private final String proteins;
    private final String vitamins;
    private final int orderCount;

    // Private constructor (use Builder for creation)
    private Dish(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.weight = builder.weight;
        this.imageUrl = builder.imageUrl;
        this.description = builder.description;
        this.dishType = builder.dishType;
        this.state = builder.state;
        this.calories = builder.calories;
        this.carbohydrates = builder.carbohydrates;
        this.fats = builder.fats;
        this.proteins = builder.proteins;
        this.vitamins = builder.vitamins;
        this.orderCount = builder.orderCount;
    }

    // Getters (optional: if you need to expose field values)
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getWeight() {
        return weight;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getDishType() {
        return dishType;
    }

    public String getState() {
        return state;
    }

    public String getCalories() {
        return calories;
    }

    public String getCarbohydrates() {
        return carbohydrates;
    }

    public String getFats() {
        return fats;
    }

    public String getProteins() {
        return proteins;
    }

    public String getVitamins() {
        return vitamins;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public static Dish fromMap(Map<String, Object> attributes) {
        return new Builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .price((String) attributes.get("price"))
                .weight((String) attributes.get("weight"))
                .imageUrl((String) attributes.get("imageUrl"))
                .calories((String) attributes.get("calories"))
                .carbohydrates((String) attributes.get("carbohydrates"))
                .fats((String) attributes.get("fats"))
                .proteins((String) attributes.get("proteins"))
                .vitamins((String) attributes.get("vitamins"))
                .dishType((String) attributes.get("dishType"))
                .description((String) attributes.get("description"))
                .state((String) attributes.get("state"))
                .orderCount(attributes.get("orderCount") != null ? Integer.parseInt(attributes.get("orderCount").toString()) : 0)
                .build();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish dishDTO = (Dish) o;
        return orderCount == dishDTO.orderCount &&
                Objects.equals(id, dishDTO.id) &&
                Objects.equals(name, dishDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, orderCount);
    }

    @Override
    public String toString() {
        return "DishDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", weight='" + weight + '\'' +
                '}';
    }

    public static class Builder {
        private String id;
        private String name;
        private String price;
        private String weight;
        private String imageUrl;
        private String description;
        private String dishType;
        private String state;
        private String calories;
        private String carbohydrates;
        private String fats;
        private String proteins;
        private String vitamins;
        private int orderCount;

        public Builder() {}


        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(String price) {
            this.price = price;
            return this;
        }

        public Builder weight(String weight) {
            this.weight = weight;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder dishType(String dishType) {
            this.dishType = dishType;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder calories(String calories) {
            this.calories = calories;
            return this;
        }

        public Builder carbohydrates(String carbohydrates) {
            this.carbohydrates = carbohydrates;
            return this;
        }

        public Builder fats(String fats) {
            this.fats = fats;
            return this;
        }

        public Builder proteins(String proteins) {
            this.proteins = proteins;
            return this;
        }

        public Builder vitamins(String vitamins) {
            this.vitamins = vitamins;
            return this;
        }

        public Builder orderCount(int orderCount) {
            this.orderCount = orderCount;
            return this;
        }

        public Dish build() {
            return new Dish(this);
        }
    }
}