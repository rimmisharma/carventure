package com.carventure.webapp.model;

import jakarta.validation.constraints.NotBlank;

public class SellerDetailsRequest {

    @NotBlank
    private String firstName;
    private String middleName; // Optional
    @NotBlank
    private String lastName;
    @NotBlank
    private String city;
    @NotBlank
    private String pincode;

    // Getters and Setters
    public @NotBlank String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public @NotBlank String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank String getCity() {
        return city;
    }

    public void setCity(@NotBlank String city) {
        this.city = city;
    }

    public @NotBlank String getPincode() {
        return pincode;
    }

    public void setPincode(@NotBlank String pincode) {
        this.pincode = pincode;
    }
}
