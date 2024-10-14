package com.carventure.webapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Field;

public class VerifyOtpRequest {

    @NotBlank(message = "Mobile number is required.")
    @Size(min = 10, max = 10, message = "10-digit valid mobile number is required.")
    @Pattern(regexp = "\\d{10}", message = "Mobile number must be exactly 10 digits long and contain only numbers.")
    @JsonProperty("mobile_number") // For JSON serialization
    @Field("mobile_number")
    private String mobileNumber;

    @NotBlank(message = "OTP is required.")
    @Size(min = 6, max = 6, message = "Invalid OTP format. OTP should be 6 digits.")
    @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits long and contain only numbers.")
    private String otpInput;

    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "Email should be valid.")
    @JsonProperty("email") // For JSON serialization
    @Field("email")
    private String email;

    public @NotBlank(message = "Mobile number is required.") @Size(min = 10, max = 10, message = "10-digit valid mobile number is required.") String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(@NotBlank(message = "Mobile number is required.") @Size(min = 10, max = 10, message = "10-digit valid mobile number is required.") String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public @NotBlank(message = "OTP is required.") @Size(min = 6, max = 6, message = "Invalid OTP format. OTP should be 6 digits.") String getOtpInput() {
        return otpInput;
    }

    public void setOtpInput(@NotBlank(message = "OTP is required.") @Size(min = 6, max = 6, message = "Invalid OTP format. OTP should be 6 digits.") String otpInput) {
        this.otpInput = otpInput;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
