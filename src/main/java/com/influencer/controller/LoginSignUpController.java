package com.influencer.controller;

import com.influencer.constants.ApplicationConstants;
import com.influencer.model.*;
import com.influencer.repository.AuthorityRepository;
import com.influencer.repository.InfluencerRepository;
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
import java.time.LocalDateTime;
import java.util.Comparator;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class LoginSignUpController {

    private final InfluencerRepository customerRepository;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final EmailService emailService ;
    private final InfluencerService influencerService;
    private final RegistrationOtpRepository registrationOtpRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;




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



}
