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
@Document(collection = "registration_otp")
public class RegistrationOtp {

    @Id
    private int id;
    private String email;
    private String password;
    private String otp;
    private Instant expirationTime; // Expiry time of the OTP is 10 minutes
    private boolean isUsed = false; // Flag to mark whether the token has been used
    private LocalDateTime createdDate;

}
