package com.influencer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "passwordResetToken")
public class PasswordResetOtp {

    @Id
    private int id;

    private String email; // Email of the user requesting password reset

    private String otp; // OTP sent to the user

    private String resetToken; // Unique reset token

    private Instant expirationTime; // Expiry time of the OTP/token

    private boolean isUsed = false; // Flag to mark whether the token has been used

    private Date createdAt;


}