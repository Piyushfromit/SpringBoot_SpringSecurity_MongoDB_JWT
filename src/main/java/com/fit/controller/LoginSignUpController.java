package com.fit.controller;

import com.fit.constants.ApplicationConstants;
import com.fit.entity.User;
import com.fit.entity.ForgetPasswordOtp;
import com.fit.entity.RegistrationOtp;
import com.fit.model.*;
import com.fit.repository.UserRepository;
import com.fit.repository.ForgetPasswordOtpRepository;
import com.fit.repository.RegistrationOtpRepository;
import com.fit.service.LoginSignUpService;
import com.fit.serviceImpl.EmailService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/api-v1")
public class LoginSignUpController {

    private final UserRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final EmailService emailService ;
    private final LoginSignUpService userService;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final ForgetPasswordOtpRepository passwordResetOtpRepository;



    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegistrationRequestDTO request) {
        try {
            Optional<User> existingUser = customerRepository.findByEmail(request.email());
            if (existingUser.isPresent()) {
                ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Account with "+request.email()+" already exists", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }
            String otp = emailService.generateCode();
            try {
                emailService.sendRegistrationOtpOnMail(request.email(), otp);
                userService.saveRegistrationOtp(request, otp );
            } catch (Exception e) {
                ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error sending mail. Please try again.", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Registration OTP sent to your email.", otp);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred while registering user.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyCode(@Valid @RequestBody OtpVerificationRequestDTO request) {
        Optional<User> existingUser = customerRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Account with "+request.email()+" already exists", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        // List<RegistrarionOtp> storedOtpPojo = registrationOtpRepository.findAllByEmail(request.getEmail());
        RegistrationOtp latestRegistrationOtp = registrationOtpRepository.findAllByEmail(request.email()).stream()
                .sorted(Comparator.comparing(RegistrationOtp::getCreatedDate).reversed())
                .findFirst()
                .orElse(null);

        if (latestRegistrationOtp == null) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), 400, "OTP not found. Please try again.", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        if (!latestRegistrationOtp.getOtp().equals( request.otp())) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), 400, "Invalid OTP. Please try again.", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        Instant expirationTime = latestRegistrationOtp.getExpirationTime();
        if (Instant.now().isAfter(expirationTime)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has expired. Please request a new one.", null));
        }

        if (latestRegistrationOtp.isUsed()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has already been used.", null));
        }
        userService.registerUser(latestRegistrationOtp);
        latestRegistrationOtp.setUsed(true);
        registrationOtpRepository.save(latestRegistrationOtp);
        ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Registration successful. Please login to continue.", null);
        return ResponseEntity.ok(apiResponse);
    }


    @RequestMapping("/user")
    public User getUserDetailsAfterLogin(Authentication authentication) {
        Optional<User> optionalCustomer = customerRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }


    @PostMapping("/generate-token")  //Login API
    public ResponseEntity<LoginResponseDTO> apiLogin (@Valid @RequestBody LoginRequestDTO request) {
        String jwt = "";
        Optional<User> userData = Optional.empty();

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password());
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);
        if(null != authenticationResponse && authenticationResponse.isAuthenticated()) {
            if (null != env) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                 jwt = Jwts.builder().issuer("User").subject("JWT Token")
                        .claim("username", authenticationResponse.getName())
                        .claim("authorities", authenticationResponse.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        .issuedAt(new java.util.Date())
                        .expiration(new java.util.Date((new java.util.Date()).getTime() + 86400000))
                        .signWith(secretKey).compact();
                userData = customerRepository.findByEmail(authentication.getName());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).header(ApplicationConstants.JWT_HEADER,jwt)
                .body(new LoginResponseDTO(HttpStatus.OK.getReasonPhrase(), jwt, userData.orElse(null)));
    }


    @PostMapping("/send-forget-pwd-otp")
    public ResponseEntity<ApiResponse> forgetPassword( @Valid @RequestBody ForgetPasswordEmailRequestDTO request) {
        try {
            String userEmail = request.email();

            Optional<User> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (!optionalCustomer.isPresent()) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "User not Registered with " + userEmail, null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            User customer = optionalCustomer.get();
            String otp = emailService.generateCode();
            emailService.sendForgetPasswordOtpOnMailUsingSMTP("User", userEmail, otp);
            //emailService.sendForgetPasswordOtpOnMailUsingSES_AWS(customer.getName(), userEmail, otp);
            userService.saveForgetPasswordOtp(userEmail, otp);

            ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Forget password OTP sent to email.", otp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
            return ResponseEntity.internalServerError().body(response);
        }
    }



    @PostMapping("/verify-forget-pwd-otp")
    public ResponseEntity<ApiResponse> verifyForgetPasswordOtp( @Valid @RequestBody OtpVerificationRequestDTO request) {
        try {
            String userEmail = request.email();
            String userOtp = request.otp();


            Optional<User> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (!optionalCustomer.isPresent()) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), 404, "User not Registered with " + userEmail, null);
                return ResponseEntity.status(404).body(response);
            }

            ForgetPasswordOtp latestRegistrationOtp = passwordResetOtpRepository.findAllByEmail(userEmail).stream()
                    .sorted(Comparator.comparing(ForgetPasswordOtp::getCreatedDate).reversed())
                    .findFirst()
                    .orElseThrow(() -> new Exception("OTP not found. Please try again."));

            if (!latestRegistrationOtp.getOtp().equals(userOtp)) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), 400, "Invalid OTP. Please try again.", null);
                return ResponseEntity.badRequest().body(response);
            }
            Instant expirationTime = latestRegistrationOtp.getExpirationTime();
            if (Instant.now().isAfter(expirationTime)) {
                ApiResponse response = new ApiResponse( LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has expired. Please request a new one.", null);
                return ResponseEntity.badRequest().body(response);
            }
            // Generate the reset token (UUID or a secure token)
            String resetToken = UUID.randomUUID().toString();  // You can use UUID or any other secure random string generator
            userService.updateForgetPasswordOtp(latestRegistrationOtp, resetToken);
            ApiResponse response = new ApiResponse(LocalDateTime.now(), 200, "OTP verified successfully. Reset token generated.", resetToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(LocalDateTime.now(), 400, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/reset-forget-pwd")
    public ResponseEntity<ApiResponse> resetPassword( @Valid  @RequestBody ForgetPasswordRequestDTO request) {
        try {
            String userEmail = request.email();
            String newPassword = request.newPassword();
            String resetToken = request.resetToken();

            // Check if the customer exists
            Optional<User> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (optionalCustomer.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "User not Registered with " + userEmail, null));
            }

            // Get the latest OTP for the user
            ForgetPasswordOtp latestRegistrationOtp = passwordResetOtpRepository.findAllByEmail(userEmail).stream()
                    .sorted(Comparator.comparing(ForgetPasswordOtp::getCreatedDate).reversed())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new one."));

            // Validate reset token
            if (resetToken == null || !resetToken.equals(latestRegistrationOtp.getResetToken())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Invalid or missing reset token.", null));
            }
            // Validate OTP expiration and usage
            Instant expirationTime = latestRegistrationOtp.getExpirationTime();
            if (Instant.now().isAfter(expirationTime)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has expired. Please request a new one.", null));
            }
            if (latestRegistrationOtp.isUsed()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Token already been used.", null));
            }

            // Proceed with password reset
            boolean isResetPassword = userService.resetForgetPassword(userEmail, newPassword);
            if (isResetPassword) {
                // Mark OTP as used after successful password reset
                latestRegistrationOtp.setUsed(true);
                passwordResetOtpRepository.save(latestRegistrationOtp);
                return ResponseEntity.ok(new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Password changed successfully.", null));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Failed to change password. Please try again.", null));
            }
        } catch (Exception e) {
            // Catch and log unexpected errors
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), e.getMessage(), null));
        }
    }



    @PostMapping("/update-password")
    public ResponseEntity<ApiResponse> updatePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequestDTO request) {
        String userEmail = authentication.getName();
        ResponseEntity<ApiResponse> isPasswordChanged  = userService.changeUserPassword(userEmail, request.oldPassword(), request.newPassword());
        return isPasswordChanged;
    }




}
