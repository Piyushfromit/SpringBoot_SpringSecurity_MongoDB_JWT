package com.fit.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @Email(message = "Invalid email address")
        @NotBlank(message = "Email is required")
        String email,

//        @Pattern(
//                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$",
//                message = "Password must be strong"
//        )
//        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
//        @NotBlank(message = "Password is required")
        String password
){}