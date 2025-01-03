package com.influencer.controller;

import com.influencer.constants.ApplicationConstants;
import com.influencer.model.*;
import com.influencer.repository.AuthorityRepository;
import com.influencer.repository.InfluencerRepository;
import com.influencer.repository.PasswordResetOtpRepository;
import com.influencer.repository.RegistrationOtpRepository;
import com.influencer.service.InfluencerService;
import com.influencer.serviceImpl.EmailService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/api-v1")
public class LoginSignUpController {

    private final InfluencerRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final EmailService emailService ;
    private final InfluencerService influencerService;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetOtpRepository passwordResetOtpRepository;



    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerInfluencer(@Valid @RequestBody RegistrationOtp registrationOtp) {
        try {
            Optional<Influencer> existingInfluencer = customerRepository.findByEmail(registrationOtp.getEmail());
            if (existingInfluencer.isPresent()) {
                ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Account with "+registrationOtp.getEmail()+" already exists", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }
            String otp = emailService.generateCode();
            try {
                emailService.sendRegistrationOtpOnMail(registrationOtp.getEmail(), otp);
                influencerService.saveRegOtpToDB(registrationOtp, otp );
            } catch (Exception e) {
                ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error sending mail. Please try again.", null);
                return ResponseEntity.badRequest().body(apiResponse);
            }

            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Registration OTP sent to your email.", otp);
            return ResponseEntity.ok(apiResponse);

        } catch (Exception ex) {
            ex.printStackTrace();
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error occurred while registering influencer.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyCode(@RequestBody VerifyRegCodeRequest verifyCodeRequest) {
        Optional<Influencer> existingInfluencer = customerRepository.findByEmail(verifyCodeRequest.getEmail());
        if (existingInfluencer.isPresent()) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Account with "+verifyCodeRequest.getEmail()+" already exists", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        // List<RegistrarionOtp> storedOtpPojo = registrationOtpRepository.findAllByEmail(verifyCodeRequest.getEmail());
        RegistrationOtp latestRegistrationOtp = registrationOtpRepository.findAllByEmail(verifyCodeRequest.getEmail()).stream()
                .sorted(Comparator.comparing(RegistrationOtp::getCreatedDate).reversed())
                .findFirst()
                .orElse(null);

        if (latestRegistrationOtp == null) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), 400, "OTP not found. Please try again.", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        if (!latestRegistrationOtp.getOtp().equals( verifyCodeRequest.getOtp())) {
            ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), 400, "Invalid OTP. Please try again.", null);
            return ResponseEntity.badRequest().body(apiResponse);
        }
        Instant expirationTime = latestRegistrationOtp.getExpirationTime();
        if (Instant.now().isAfter(expirationTime)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has expired. Please request a new one.", null));
        }
        influencerService.registerInfluencer(latestRegistrationOtp);
        ApiResponse apiResponse = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Registration successful. Please login to continue.", null);
        return ResponseEntity.ok(apiResponse);
    }


    @RequestMapping("/user")
    public Influencer getUserDetailsAfterLogin(Authentication authentication) {
        System.out.println(authentication.getName());
        Optional<Influencer> optionalCustomer = customerRepository.findByEmail(authentication.getName());
        return optionalCustomer.orElse(null);
    }


    @PostMapping("/generate-token")
    public ResponseEntity<LoginResponseDTO> apiLogin (@RequestBody LoginRequestDTO loginRequest) {
        String jwt = "";
        Optional<Influencer> userData = Optional.empty();

        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.email(), loginRequest.password());
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);
        if(null != authenticationResponse && authenticationResponse.isAuthenticated()) {
            if (null != env) {
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                 jwt = Jwts.builder().issuer("Influencer").subject("JWT Token")
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
    public ResponseEntity<ApiResponse> forgetPassword( @RequestBody ForgetPasswordRequest forgetPasswordRequest) {
        try {
            String userEmail = forgetPasswordRequest.email();

            Optional<Influencer> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (!optionalCustomer.isPresent()) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "User not Registered with " + userEmail, null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            Influencer customer = optionalCustomer.get();
            String otp = emailService.generateCode();
            System.out.println("Forget password otp: "+ otp);
            emailService.sendForgetPasswordOtpOnMailUsingSMTP(customer.getName(), userEmail, otp);
            //emailService.sendForgetPasswordOtpOnMailUsingSES_AWS(customer.getName(), userEmail, otp);
            influencerService.savePasswordResetOtp(userEmail, otp);

            ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.OK.value(), "Forget password request sent successfully. OTP sent to email.", otp);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), null);
            return ResponseEntity.internalServerError().body(response);
        }
    }



    @PostMapping("/verify-forget-pwd-otp")
    public ResponseEntity<ApiResponse> verifyForgetPasswordOtp(Authentication authentication, @RequestBody VerifyRegCodeRequest verifyOtpRequest) {
        try {
            String userEmail = verifyOtpRequest.getEmail();
            String userOtp = verifyOtpRequest.getOtp();


            Optional<Influencer> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (!optionalCustomer.isPresent()) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), 404, "Customer not found", null);
                return ResponseEntity.status(404).body(response);
            }

            PasswordResetOtp latestRegistrationOtp = passwordResetOtpRepository.findAllByEmail(userEmail).stream()
                    .sorted(Comparator.comparing(PasswordResetOtp::getCreatedAt).reversed())
                    .findFirst()
                    .orElseThrow(() -> new Exception("OTP not found. Please try again."));

            Instant expirationTime = latestRegistrationOtp.getExpirationTime();
            if (Instant.now().isAfter(expirationTime)) {
                ApiResponse response = new ApiResponse( LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has expired. Please request a new one.", null);
                return ResponseEntity.badRequest().body(response);
            }
            if (!latestRegistrationOtp.getOtp().equals(userOtp)) {
                ApiResponse response = new ApiResponse(LocalDateTime.now(), 400, "Invalid OTP. Please try again.", null);
                return ResponseEntity.badRequest().body(response);
            }
            // Generate the reset token (UUID or a secure token)
            String resetToken = UUID.randomUUID().toString();  // You can use UUID or any other secure random string generator
            influencerService.updatePasswordResetOtp(latestRegistrationOtp, resetToken);
            ApiResponse response = new ApiResponse(LocalDateTime.now(), 200, "OTP verified successfully. Reset token generated.", resetToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(LocalDateTime.now(), 400, e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping("/reset-forget-pwd")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        try {
            String userEmail = resetPasswordRequest.email();
            String newPassword = resetPasswordRequest.newPassword();
            String resetToken = resetPasswordRequest.resetToken();

            // Check if the customer exists
            Optional<Influencer> optionalCustomer = customerRepository.findByEmail(userEmail);
            if (optionalCustomer.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Customer not found", null));
            }

            // Get the latest OTP for the user
            PasswordResetOtp latestRegistrationOtp = passwordResetOtpRepository.findAllByEmail(userEmail).stream()
                    .sorted(Comparator.comparing(PasswordResetOtp::getCreatedAt).reversed())
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
                        .body(new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "OTP has already been used.", null));
            }

            // Proceed with password reset
            boolean isResetPassword = influencerService.resetInfluencerPwd(userEmail, newPassword);
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




}
