package com.restaurantapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.util.Objects;

public final class SignIn {

    private final String email;
    private final String password;

    public SignIn(@JsonProperty("email") String email, @JsonProperty("password") String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SignIn signIn = (SignIn) o;
        return Objects.equals(email, signIn.email) && Objects.equals(password, signIn.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "SignIn{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
