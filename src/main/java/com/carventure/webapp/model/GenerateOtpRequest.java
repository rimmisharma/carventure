package com.carventure.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Field;

public class GenerateOtpRequest {
    @NotBlank(message = "Mobile number is required.")
    @Size(min = 10, max = 10, message = "10-digit valid mobile number is required.")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be exactly 10 digits long and contain only numbers.")
    @JsonProperty("mobile_number") // For JSON serialization
    @Field("mobile_number")
    private String mobileNumber;

    // Email is optional
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email should be valid.")
    @JsonProperty("email") // For JSON serialization
    @Field("email")
    private String email;

    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
