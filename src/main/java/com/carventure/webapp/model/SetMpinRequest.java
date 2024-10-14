package com.carventure.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Field;

public class SetMpinRequest {
    @NotBlank(message = "Mobile number is required.")
    @Size(min = 10, max = 10, message = "10-digit valid mobile number is required.")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be exactly 10 digits long and contain only numbers.")
    @JsonProperty("mobile_number") // For JSON serialization
    @Field("mobile_number")
    private String mobileNumber;

    @NotBlank(message = "MPIN is required")
    @Size(min = 6, max = 6, message = "MPIN must be exactly 6 digits")
    @Pattern(regexp = "\\d{6}", message = "MPIN must contain only digits")
    private String mpin;

    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMpin() {
        return mpin;
    }

    public void setMpin(String mpin) {
        this.mpin = mpin;
    }
}
