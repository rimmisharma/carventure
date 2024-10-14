package com.carventure.webapp.service;

import com.carventure.webapp.exception.CooldownActiveException;
import com.carventure.webapp.exception.InvalidOtpException;
import com.carventure.webapp.exception.OtpExpiredException;
import com.carventure.webapp.exception.SellerNotFoundException;
import com.carventure.webapp.model.Seller;
import com.carventure.webapp.model.SellerDetailsRequest;
import com.carventure.webapp.repository.SellerRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private static final Random RANDOM = new Random();
    private static final Logger logger = LoggerFactory.getLogger(SellerService.class);

    @Value("${golden.otp.value}")
    private String goldenOtpValue;

    @Value("${otp.retry.count}")
    private int otpRetryCount;

    @Value("${otp.cooloff.time}")
    private int otpCooloffTime;

    @Value("${otp.expiry.time}")
    private int otpExpiryTime;

    private final JavaMailSender mailSender;

    private Environment environment;

    private static final String OTP_SENT = "OTP sent successfully";

    @Autowired
    public  SellerService(SellerRepository sellerRepository, JavaMailSender javaMailSender, Environment environment) {
        this.sellerRepository = sellerRepository;
        this.mailSender = javaMailSender;
        this.environment = environment;
    }

    public String sendPhoneOtp(String phoneNumber) {
        //get seller by mobile number
        Result result = getOtp(phoneNumber);
        Seller seller;
        if (result.sellerOptional.isPresent()) {
            seller = result.sellerOptional.get();
            if (seller.getOtpRetryCount() >= otpRetryCount) { //if seller attempted more than 5
                if (LocalDateTime.now().isBefore(seller.getOtpCoolOffEndTime())) {
                    throw new CooldownActiveException("seller is still in the cooldown period. Please try again later");
                } else {
                    seller.setOtp(result.hashedOtp);
                    seller.setOtpExpiryDate(LocalDateTime.now().plusMinutes(otpExpiryTime));
                    seller.setOtpRetryCount(1);
                    //sendMobileMessage();
                    sellerRepository.save(seller);
                    return OTP_SENT;
                }
            } else { // has not attempted more than 5 times
                seller.setOtpRetryCount(seller.getOtpRetryCount() + 1);
                if (seller.getOtpRetryCount() == 5) {
                    seller.setOtpCoolOffEndTime(LocalDateTime.now().plusMinutes(otpCooloffTime));
                }
                seller.setOtp(result.hashedOtp);
                seller.setOtpExpiryDate(LocalDateTime.now().plusMinutes(otpExpiryTime));
                //sendMobileMessage();
                sellerRepository.save(seller);
                return OTP_SENT;
            }

        } else { //if new seller
            seller = new Seller(phoneNumber, result.hashedOtp, 1, LocalDateTime.now().plusMinutes(otpExpiryTime));
            //sendMobileMessage();
            sellerRepository.save(seller);
            return OTP_SENT;
        }
    }

    private Result getOtp(String phoneNumber) {
        Optional<Seller> sellerOptional = sellerRepository.findByPhone(phoneNumber);
        String otp;
        String activeProfile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "";
        if ("localhost".equals(activeProfile)) {
            otp = goldenOtpValue;
        } else {
            otp = generateOtp();
        }
        // Hash the OTP
        String hashedOtp = hashOtp(otp);
        return new Result(sellerOptional, otp, hashedOtp);
    }

    private static class Result {
        public final Optional<Seller> sellerOptional;
        public final String otp;
        public final String hashedOtp;

        public Result(Optional<Seller> sellerOptional, String otp, String hashedOtp) {
            this.sellerOptional = sellerOptional;
            this.otp = otp;
            this.hashedOtp = hashedOtp;
        }
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(10000));
    }

    private String hashOtp(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString(); // Return hashed OTP as hex string
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing OTP", e);
        }
    }

    public boolean verifyPhoneOtp(String phoneNumber, String otpInput) {
        return verifyOtp(phoneNumber, otpInput, "mobile");
    }

    private boolean verifyOtp(String phoneNumber, String otpInput, String entityInVerification) {
        Optional<Seller> sellerOptional = sellerRepository.findByPhone(phoneNumber);
        Seller seller;
        if (sellerOptional.isPresent()) {
            seller = sellerOptional.get();
            if (LocalDateTime.now().isBefore(seller.getOtpExpiryDate())) {
                // Hash the OTP
                String hashedOtp = hashOtp(otpInput);
                if (hashedOtp.equals(seller.getOtp())) {
                    seller.setOtp(null);
                    seller.setOtpRetryCount(1);
                    seller.setOtpExpiryDate(null);
                    seller.setOtpCoolOffEndTime(null);
                    if (entityInVerification.equals("mobile")) {
                        seller.setCurrentState(Seller.CurrentState.MOBILE_VERIFIED.toString());
                    } else if (entityInVerification.equals("email")) {
                        seller.setCurrentState(Seller.CurrentState.EMAIL_VERIFIED.toString());
                    }
                    sellerRepository.save(seller);
                    return true;
                } else {
                    throw new InvalidOtpException("OTP does not match");
                }
            } else {
                throw new OtpExpiredException("OTP expired, please retry with a new one");
            }
        }
        throw new SellerNotFoundException("Internal server error, seller not found. Try getting a new OTP");
    }

    // Method to generate JWT token
    public String generateToken(String phoneNumber) {
        return Jwts.builder()
                .setSubject(phoneNumber)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token valid for 1 day
                .signWith(SignatureAlgorithm.HS512, "b2F8c7eR3t6Qp0zL9vX5gY4hK1jN8sM7wP0oU4fH2kR9aD6cQ3tY8zV4wP5eF7gU".getBytes()) // Replace with your secret key
                .compact();
    }

    public String extractPhoneNumberFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey("b2F8c7eR3t6Qp0zL9vX5gY4hK1jN8sM7wP0oU4fH2kR9aD6cQ3tY8zV4wP5eF7gU".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
        logger.info("Current seller is: {}", claims.getSubject());
        return claims.getSubject();
    }

    public String sendEmailOtp(String email, String sellerPhoneNumber) {

        checkseller(sellerPhoneNumber);

        Result result = getOtp(sellerPhoneNumber);
        Seller seller;
        if (result.sellerOptional.isPresent()) {
            seller = result.sellerOptional.get();
            if (seller.getOtpRetryCount() >= otpRetryCount) { // If seller attempted more than 5
                if (LocalDateTime.now().isBefore(seller.getOtpCoolOffEndTime())) {
                    throw new CooldownActiveException("seller is still in the cooldown period. Please try again later");
                } else {
                    seller.setOtp(result.hashedOtp);
                    seller.setOtpExpiryDate(LocalDateTime.now().plusMinutes(otpExpiryTime));
                    seller.setOtpRetryCount(1);
                    sendEmail(email, result.otp); // Send OTP email
                    sellerRepository.save(seller);
                    return OTP_SENT;
                }
            } else { // Has not attempted more than 5 times
                seller.setOtpRetryCount(seller.getOtpRetryCount() + 1);
                if (seller.getOtpRetryCount() == 5) {
                    seller.setOtpCoolOffEndTime(LocalDateTime.now().plusMinutes(otpCooloffTime));
                }
                seller.setOtp(result.hashedOtp);
                seller.setOtpExpiryDate(LocalDateTime.now().plusMinutes(otpExpiryTime));
                seller.setEmail(email);
                sendEmail(email, result.otp); // Send OTP email
                sellerRepository.save(seller);
                return OTP_SENT;
            }
        } else {
            throw new SellerNotFoundException("Internal server error, seller not found. Try starting a new mobile number verification process for adding new seller");
        }
    }

    private static void checkseller(String sellerPhoneNumber) {
        if (StringUtils.isBlank(sellerPhoneNumber)) {
            throw new SellerNotFoundException("Not able to find any valid seller details from token");
        }
    }
    public boolean sellerExists (String sellerPhoneNumber) {
        Optional<Seller> sellerOptional = sellerRepository.findByPhone(sellerPhoneNumber);
        return sellerOptional.isPresent();
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("gurpreet.singh.ness@gmail.com");
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp);
        mailSender.send(message);
    }

    public boolean verifyEmailOTP(String email, String otpInput, String sellerPhoneNumber) {

        checkseller(sellerPhoneNumber);
        if (sellerRepository.findByEmail(email).isEmpty()) {
            throw new SellerNotFoundException("Invalid email address sent");
        }
        return verifyOtp(sellerPhoneNumber, otpInput, "email");
    }

    public Seller saveSellerDetails(SellerDetailsRequest detailsRequest, String token) {
        String phoneNumber = extractPhoneNumberFromToken(token);

        Optional<Seller> optionalSeller = sellerRepository.findByPhone(phoneNumber);

        if (optionalSeller.isPresent()) {
            Seller seller = optionalSeller.get();
            // Set the seller's details
            seller.setFirstName(detailsRequest.getFirstName());
            seller.setMiddleName(detailsRequest.getMiddleName());
            seller.setLastName(detailsRequest.getLastName());
            seller.setCity(detailsRequest.getCity());
            seller.setPincode(detailsRequest.getPincode());
            seller.setCurrentState(Seller.CurrentState.REGISTRATION_COMPLETED.toString());

            // Save updated seller
            return sellerRepository.save(seller);
        } else {
            throw new SellerNotFoundException("Seller not found for phone number: " + phoneNumber);
        }
    }
}
