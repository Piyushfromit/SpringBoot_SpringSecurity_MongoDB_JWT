package com.fit.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "forget_password_otp")
public class ForgetPasswordOtp {

    @Id
    private Long id;
    private String email; // Email of the user requesting password reset
    private String resetToken; // Unique reset token
    private String otp; // OTP sent to the user
    private Instant expirationTime; // Expiry time of the OTP/token
    private boolean isUsed = false; // Flag to mark whether the token has been used
    private LocalDateTime createdDate;


}