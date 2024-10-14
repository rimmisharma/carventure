package com.carventure.webapp.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "sellers")
public class Seller {
    public enum CurrentState {
        //small case stages mobile_verified, email_verified, preferred_name_filled
        MOBILE_VERIFIED("mobile_verified"),
        EMAIL_VERIFIED("email_verified"),
        REGISTRATION_COMPLETED("registration_completed");

        private final String value;
        CurrentState(String value) {
            this.value = value;
        }
        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Id
    private String id;
    @NotBlank
    private String firstName;
    private String middleName;
    @NotBlank
    private String lastName;
    private String email;
    private String phone;
    private String city;
    private String pincode;
    private String otp;
    private int otpRetryCount;
    private LocalDateTime otpExpiryDate;
    private LocalDateTime otpCoolOffEndTime;
    private String currentState;

    public Seller( String phone, String otp, int otpRetryCount, LocalDateTime otpExpiryDate) {
        this.phone = phone;
        this.otp = otp;
        this.otpRetryCount = otpRetryCount;
        this.otpExpiryDate = otpExpiryDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public int getOtpRetryCount() {
        return otpRetryCount;
    }

    public void setOtpRetryCount(int otpRetryCount) {
        this.otpRetryCount = otpRetryCount;
    }

    public LocalDateTime getOtpExpiryDate() {
        return otpExpiryDate;
    }

    public void setOtpExpiryDate(LocalDateTime otpExpiryDate) {
        this.otpExpiryDate = otpExpiryDate;
    }

    public LocalDateTime getOtpCoolOffEndTime() {
        return otpCoolOffEndTime;
    }

    public void setOtpCoolOffEndTime(LocalDateTime otpCoolOffEndTime) {
        this.otpCoolOffEndTime = otpCoolOffEndTime;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
