package com.restaurantapp.dto;

import jakarta.validation.constraints.NotBlank;

public class UserProfileUpdateRequest {
    @NotBlank(message = "FirstName should not be blank")
    private String firstName;
    @NotBlank(message = "LastName should not be Blank")
    private String lastName;
    private String base64encodedImage;

    public UserProfileUpdateRequest(String firstName, String lastName, String base64encodedImage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.base64encodedImage = base64encodedImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBase64encodedImage() {
        return base64encodedImage;
    }

    public void setBase64encodedImage(String base64encodedImage) {
        this.base64encodedImage = base64encodedImage;
    }
}
