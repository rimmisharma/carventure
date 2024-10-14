package com.carventure.webapp.controller;

import com.carventure.webapp.exception.CooldownActiveException;
import com.carventure.webapp.exception.InvalidOtpException;
import com.carventure.webapp.exception.OtpExpiredException;
import com.carventure.webapp.exception.SellerNotFoundException;
import com.carventure.webapp.model.*;
import com.carventure.webapp.service.SellerService;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers/")
public class SellerController {

    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);
    private final SellerService sellerService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("generatePhoneOtp")
    public ResponseEntity<ApiResponse> generateOTP(@Valid @RequestBody GenerateOtpRequest generateOtpRequest) {
        try {
            String phoneNumber = generateOtpRequest.getMobileNumber();
            logger.info("Generating OTP for phone number {}", phoneNumber);

            String message = sellerService.sendPhoneOtp(phoneNumber);
            logger.info(message);

            return ResponseEntity.ok(new ApiResponse(message, "success"));
        } catch (CooldownActiveException e) {
            logger.error("Cooldown active exception", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Cooldown active. Please wait before retrying.", "failed", true));
        } catch (Exception e) {
            logger.error("An error occurred while generating OTP", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred while generating OTP. Please try again later.", "failed", true));
        }
    }

    @PostMapping("verifyPhoneOtp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        String phoneNumber = verifyOtpRequest.getMobileNumber();
        String otpInput = verifyOtpRequest.getOtpInput();
        logger.info("Verifying OTP for phone number {}", phoneNumber);

        try {
            if (sellerService.verifyPhoneOtp(phoneNumber, otpInput)) {
                logger.info("OTP verification successful for phone number {}", phoneNumber);
                return ResponseEntity.ok(new ApiResponse("OTP verified successfully.", "success"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Invalid OTP", "failed", true));
            }
        } catch (InvalidOtpException e) {
            logger.error("Invalid OTP for phone number {}: {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid OTP", "failed", true));
        } catch (OtpExpiredException e) {
            logger.error("OTP expired for phone number {}: {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("OTP has expired. Please request a new one.", "failed", true));
        } catch (SellerNotFoundException e) {
            logger.error("Seller not found for phone number {}: {}", phoneNumber, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Seller not found", "failed", true));
        } catch (Exception e) {
            logger.error("An error occurred during OTP verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred during OTP verification. Please try again later.", "failed", true));
        }
    }

    @PostMapping("generateEmailOtp")
    public ResponseEntity<ApiResponse> generateEmailOTP(@Valid @RequestBody GenerateOtpRequest generateOtpRequest, @CookieValue("token") String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Token is missing", "failed"));
        }
        try {
            String email = generateOtpRequest.getEmail();  // Assuming GenerateOtpRequest now has an 'email' field
            logger.info("Generating OTP for email {}", email);

            String userPhoneNumber = sellerService.extractPhoneNumberFromToken(token);
            String message = sellerService.sendEmailOtp(email, userPhoneNumber);
            logger.info(message);

            return ResponseEntity.ok(new ApiResponse(message, "success"));
        } catch (CooldownActiveException e) {
            logger.error("Cooldown active exception", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Cooldown active. Please wait before retrying.", "failed", true));
        } catch (SellerNotFoundException e) {
            logger.error("Seller not found. {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Seller not found", "failed", true));
        } catch (Exception e) {
            logger.error("An error occurred while generating OTP for email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred while generating OTP for email. Please try again later.", "failed", true));
        }
    }

    @PostMapping("verifyEmailOtp")
    public ResponseEntity<ApiResponse> verifyEmailOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest, @CookieValue("token") String token) {
        String email = verifyOtpRequest.getEmail();
        String otpInput = verifyOtpRequest.getOtpInput();
        logger.info("Verifying OTP for Email {}", email);

        try {
            String userPhoneNumber = sellerService.extractPhoneNumberFromToken(token);
            if (sellerService.verifyEmailOTP(email, otpInput, userPhoneNumber)) {
                logger.info("OTP verification successful for email {}", email);

                return ResponseEntity.ok().body(new ApiResponse("OTP verified successfully.", "success"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse("Invalid OTP", "failed", true));
            }
        } catch (InvalidOtpException e) {
            logger.error("Invalid OTP for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid OTP", "failed", true));
        } catch (OtpExpiredException e) {
            logger.error("OTP expired for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("OTP has expired. Please request a new one.", "failed", true));
        } catch (SellerNotFoundException e) {
            logger.error("Seller not found for email {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Seller not found", "failed", true));
        } catch (Exception e) {
            logger.error("An error occurred during OTP verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("An error occurred during OTP verification. Please try again later.", "failed", true));
        }
    }


    @PostMapping("completeRegistration")
    public ResponseEntity<ApiResponse> completeRegistration(
            @Valid @RequestBody SellerDetailsRequest request,
            @CookieValue("token") String token) {
        String sellerPhoneNumber = sellerService.extractPhoneNumberFromToken(token);

        // Check if seller exists
        if (!sellerService.sellerExists(sellerPhoneNumber)) {
            logger.error("No seller exists with phone number {}", sellerPhoneNumber);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Seller not found with phone number " + sellerPhoneNumber, "failed", true));
        }

        // Check if mandatory fields are present and valid
        if (StringUtils.isBlank(request.getFirstName()) ||
                StringUtils.isBlank(request.getLastName()) ||
                StringUtils.isBlank(request.getCity()) ||
                StringUtils.isBlank(request.getPincode())) {
            logger.error("Empty request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Request body cannot be empty", "failed"));
        }

        // Validate pincode (assuming 6 digits)
        if (!request.getPincode().matches("\\d{6}")) {
            logger.error("Invalid pincode {}", request.getPincode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid pincode. It should be 6 digits long.", "failed"));
        }

        // Validate city name (alphabetic characters only)
        if (!request.getCity().matches("^[a-zA-Z\\s]+$")) {
            logger.error("Invalid city name {}", request.getCity());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("City name can only contain letters and spaces.", "failed"));
        }

        // Validate first name and last name
        if (!request.getFirstName().matches("^[a-zA-Z]+$")) {
            logger.error("Invalid first name {}", request.getFirstName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("First name can only contain letters.", "failed"));
        }

        if (!request.getLastName().matches("^[a-zA-Z]+$")) {
            logger.error("Invalid last name {}", request.getLastName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Last name can only contain letters.", "failed"));
        }

        // Validate middle name if provided
        if (StringUtils.isNotBlank(request.getMiddleName()) &&
                !request.getMiddleName().matches("^[a-zA-Z]+$")) {
            logger.error("Invalid middle name {}", request.getMiddleName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Middle name can only contain letters if provided.", "failed"));
        }

        // Save the seller details and complete registration
        Seller updatedSeller = sellerService.saveSellerDetails(request, token);
        return ResponseEntity.ok(new ApiResponse("Seller details saved successfully.", "success"));
    }

}
